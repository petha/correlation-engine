require 'rest-client'
require 'json'
require 'csv'
require 'securerandom'

class Main
  def initialize()
  end

  def run
    puts "Running seed tool for importing a default data set"
    begin
        self.send_data("analyze", {
            name: "UniqWords",
            extractors: [
                {
                    name: "uniq_words",
                    sourceField: "description"
                }
            ]
        })
    rescue
    end

    File.readlines(ARGV[0]).map {|row| self.row_to_doc(row)}.each do |doc|
        self.send_data("index", doc)
    end

  end

  def row_to_doc(row)
    return { id: SecureRandom.uuid, fields: {
            description: row
        }
    }
  end

  def send_data(api, data)
   RestClient.post "http://localhost:8080/#{api}", data.to_json, {content_type: :json, accept: :json}
  end

end


