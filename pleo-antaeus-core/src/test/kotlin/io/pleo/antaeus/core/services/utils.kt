package io.pleo.antaeus.core.services

import org.junit.jupiter.params.provider.Arguments

internal infix fun <A, B> A.andArg(that: B): Arguments = Arguments.of(this, that)
