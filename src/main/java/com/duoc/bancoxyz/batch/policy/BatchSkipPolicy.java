package com.duoc.bancoxyz.batch.policy;

import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;

import com.duoc.bancoxyz.batch.exception.BatchValidationException;

public class BatchSkipPolicy implements SkipPolicy {

    private static final int MAX_SKIPS = 20;

    @Override
    public boolean shouldSkip(Throwable throwable, long skipCount) throws SkipLimitExceededException {
        if (throwable == null) {
            return false;
        }

        if (skipCount >= MAX_SKIPS) {
            throw new SkipLimitExceededException(MAX_SKIPS, throwable);
        }

        if (throwable instanceof BatchValidationException) {
            return true;
        }

        if (throwable instanceof IllegalArgumentException) {
            return true;
        }

        if (throwable instanceof NumberFormatException) {
            return true;
        }

        return false;
    }
}
