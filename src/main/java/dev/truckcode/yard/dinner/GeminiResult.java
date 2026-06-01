package dev.truckcode.yard.dinner;

public sealed interface GeminiResult {
    record Success(Recipe recipe)   implements GeminiResult {}
    record OverCapacity()           implements GeminiResult {}
    record Failure(Exception cause) implements GeminiResult {}
}
