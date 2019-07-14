package com.github.petha.correlationengine.model;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.function.BiConsumer;

public class DiskIndexRecordParser {
    private ParseState currentState = ParseState.ReadLength;
    private int length = 0;
    private boolean deleted = false;
    private int[][] values = null;
    private long uuidHigh = 0;
    private int currentValue = 0;

    public static int[][] readOneFromStream(InputStream stream) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(stream);
        int length = dataInputStream.readInt();
        boolean b = dataInputStream.readBoolean();
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
                case ReadLength:
                    this.length = buffer.getInt();
                    this.values = new int[2][this.length];
                    this.currentState = this.currentState.nextState();
                    break;
                case ReadDeleted:
                    this.deleted = buffer.get() != 0;
                    this.currentState = this.currentState.nextState();
                    break;
                case ReadPos:
                    this.values[0][currentValue++] = buffer.getInt();
                    if (currentValue == length) {
                        currentValue = 0;
                        this.currentState = this.currentState.nextState();
                    }
                    break;
                case ReadVal:
                    this.values[1][currentValue++] = buffer.getInt();
                    if (currentValue == length) {
                        currentValue = 0;
                        this.currentState = this.currentState.nextState();
                    }
                    break;
                case ReadHighId:
                    this.uuidHigh = buffer.getLong();
                    this.currentState = this.currentState.nextState();
                    break;
                case ReadLowId:
                    long uuidLow = buffer.getLong();
                    consumer.accept(values, new UUID(uuidHigh, uuidLow));
                    this.currentState = this.currentState.nextState();
                    break;
            }
        }
    }

    private enum ParseState {
        ReadLength {
            @Override
            public ParseState nextState() {
                return ReadDeleted;
            }

            @Override
            public int requiredLength() {
                return 4;
            }
        },
        ReadDeleted {
            @Override
            public ParseState nextState() {
                return ReadPos;
            }

            @Override
            public int requiredLength() {
                return 1;
            }
        },
        ReadPos {
            @Override
            public ParseState nextState() {
                return ReadVal;
            }

            @Override
            public int requiredLength() {
                return 4;
            }
        },
        ReadVal {
            @Override
            public ParseState nextState() {
                return ReadHighId;
            }

            @Override
            public int requiredLength() {
                return 4;
            }
        },
        ReadHighId {
            @Override
            public ParseState nextState() {
                return ReadLowId;
            }

            @Override
            public int requiredLength() {
                return 8;
            }
        },
        ReadLowId {
            @Override
            public ParseState nextState() {
                return ReadLength;
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
