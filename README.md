#The Correlation Engine

[![Maintainability](https://api.codeclimate.com/v1/badges/def17503fa5eafb0f32e/maintainability)](https://codeclimate.com/github/petha/correlation-engine/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/def17503fa5eafb0f32e/test_coverage)](https://codeclimate.com/github/petha/correlation-engine/test_coverage)

* The correlation engine is a hobby project
* It's purpose is for correlation metrics between documents
* It uses TF-IDF for similarity between term vectors
* The idea is to create a database where any type of document can be correlated to a set of documents
* It currently uses Spring as a web frontend
* Possible use cases could be for finding similar attacks, correlating CVEs, log correlation, SIEM etc 

1. Document
A datatype with a unique identifier and a set of fields with text information

2. Vector Extractor
Key role is to extract terms from text input mapped from a document field.
It does so by tokenizing the input from a document field, extracting (possibliy new) terms and building a sparse vector.

3. Analyzer
A list of vector extractors 

4. SparseVector
A vector where only the non zero fields are present

5. Dictionary
A list of terms, the number of indexed documents and the frequency of the terms in the indexed documents 

### Example of a analyzer 
```{
	"name": "uniq",
	"extractors": [
		{
			"name": "uniq_words",
			"sourceField": "description" 
		},
		{
			"name": "uniq_words",
			"sourceField": "name" 
		}
	]
}```

