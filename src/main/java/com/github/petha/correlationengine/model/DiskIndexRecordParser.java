package com.github.petha.correlationengine.model;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.function.BiConsumer;

public class DiskIndexRecordParser {
    private ParseState currentState = ParseState.READ_LENGTH;
    private int length = 0;
    private int[][] values = null;
    private long uuidHigh = 0;
    private int currentValue = 0;

    public static int[][] readOneFromStream(InputStream stream) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(stream);
        int length = dataInputStream.readInt();
        dataInputStream.readBoolean();
        int[][] result = new int[2][length];
        for (int i = 0; i < length; i++) {
            result[0][i] = dataInputStream.readInt();
        }
        for (int i = 0; i < length; i++) {
            result[1][i] = dataInputStream.readInt();
        }

        return result;
    }

    public void parse(ByteBuffer buffer, BiConsumer<int[][], UUID> consumer) {
        while (buffer.remaining() >= this.currentState.requiredLength()) {
            switch (currentState) {
                case READ_LENGTH:
                    this.length = buffer.getInt();
                    this.values = new int[2][this.length];
                    this.currentState = this.currentState.nextState();
                    break;
                case READ_DELETED:
                    buffer.get();
                    this.currentState = this.currentState.nextState();
                    break;
                case READ_POS:
                    this.values[0][currentValue++] = buffer.getInt();
                    if (currentValue == length) {
                        currentValue = 0;
                        this.currentState = this.currentState.nextState();
                    }
                    break;
                case READ_VAL:
                    this.values[1][currentValue++] = buffer.getInt();
                    if (currentValue == length) {
                        currentValue = 0;
                        this.currentState = this.currentState.nextState();
                    }
                    break;
                case READ_HIGH_ID:
                    this.uuidHigh = buffer.getLong();
                    this.currentState = this.currentState.nextState();
                    break;
                case READ_LOW_ID:
                    long uuidLow = buffer.getLong();
                    consumer.accept(values, new UUID(uuidHigh, uuidLow));
                    this.currentState = this.currentState.nextState();
                    break;
            }
        }
    }

    private enum ParseState {
        READ_LENGTH {
            @Override
            public ParseState nextState() {
                return READ_DELETED;
            }

            @Override
            public int requiredLength() {
                return 4;
            }
        },
        READ_DELETED {
            @Override
            public ParseState nextState() {
                return READ_POS;
            }

            @Override
            public int requiredLength() {
                return 1;
            }
        },
        READ_POS {
            @Override
            public ParseState nextState() {
                return READ_VAL;
            }

            @Override
            public int requiredLength() {
                return 4;
            }
        },
        READ_VAL {
            @Override
            public ParseState nextState() {
                return READ_HIGH_ID;
            }

            @Override
            public int requiredLength() {
                return 4;
            }
        },
        READ_HIGH_ID {
            @Override
            public ParseState nextState() {
                return READ_LOW_ID;
            }

            @Override
            public int requiredLength() {
                return 8;
            }
        },
        READ_LOW_ID {
            @Override
            public ParseState nextState() {
                return READ_LENGTH;
            }

            @Override
            public int requiredLength() {
                return 8;
            }
        };

        public abstract ParseState nextState();

        public abstract int requiredLength();
    }
}
