package com.duoc.bancoxyz.batch;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.duoc.bancoxyz.batch.exception.BatchValidationException;
import com.duoc.bancoxyz.batch.policy.BatchSkipPolicy;

class BatchFaultToleranceTest {

    @Test
    void skipPolicySkipsValidationAndParseErrors() throws Exception {
        BatchSkipPolicy policy = new BatchSkipPolicy();

        assertTrue(policy.shouldSkip(new BatchValidationException("invalid row"), 0));
        assertTrue(policy.shouldSkip(new IllegalArgumentException("bad numeric value"), 0));
        assertFalse(policy.shouldSkip(new IllegalStateException("fatal system failure"), 0));
    }
}
