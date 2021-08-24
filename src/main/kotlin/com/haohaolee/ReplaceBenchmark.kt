

package com.haohaolee

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import kotlin.streams.asSequence
import java.util.concurrent.*

fun replaceSpecialCharsWithLoop1(
  str: String, start: Int, end: Int, ws: Int
  ): String =
    str.codePoints()
    .map{ if (it in start..end) it else ws }
    .toArray()
    .let { String(it, 0, it.size) }

fun replaceSpecialCharsWithLoop2(
  str: String, start: Int, end: Int, ws: Int
  ): String =
    str.codePoints()
    .map{ if (it in start..end) it else ws }
    .collect(
      { StringBuilder() },
      { b,p -> b.appendCodePoint(p) },
      { b,s -> b.append(s) }
    ).toString()

fun replaceSpecialCharsWithLoop3(
  str: String, start: Int, end: Int, ws: Int
  ): String =
    str.codePoints()
    .asSequence()
    .map{ if (it in start..end) it else ws }
    .fold(StringBuilder()) {
      builder, codepoint -> builder.appendCodePoint(codepoint)
    }
    .toString()

fun replaceSpecialCharsWithLoop4(
  str: String, start: Int, end: Int, ws: Int
  ): String {
    val builder = StringBuilder()
    str.codePoints()
    .map{ if (it in start..end) it else ws }
    .forEach {
      builder.appendCodePoint(it)
    }
    return builder.toString()
  }

fun replaceSpecialCharsWithLoop5(
  str: String, start: Int, end: Int, ws: Int
  ): String {
    var builder = StringBuilder()
    var index = 0
    val length = str.length
    while (index < length) {
      val codepoint = str.codePointAt(index)
      if (codepoint in start..end)
        builder.appendCodePoint(codepoint)
      else
        builder.appendCodePoint(ws)
      index += Character.charCount(codepoint)
    }
    return builder.toString()
  }

fun replaceSpecialCharsWithRegex(
  str: String,
  unsupported: Regex,
  replacement: String
) =
  unsupported.replace(str, replacement)

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
open class ReplaceBenchmark {

    @State(Scope.Benchmark)
    open class BenchmarkState {
        val unsupported = Regex("[^ -~]")
        val startCode = ' '.code
        val endCode = '~'.code
        val wsCode = ' '.code
        val ws = " "
        var testSet : Array<String> = arrayOf()

        @Setup(Level.Trial)
        public fun setup() {
          testSet = arrayOf(
            "A very long company name",
            "A™very©long compàny®name",
            "A😢very long😁company长name",
            )
        }
    }

    @Fork(value = 1, warmups = 1)
    @Warmup(iterations = 1)
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    fun MethodLoop1(bh: Blackhole, state: BenchmarkState) {
        for (str in state.testSet)
          bh.consume(replaceSpecialCharsWithLoop1(str, state.startCode, state.endCode, state.wsCode))
    }

    @Fork(value = 1, warmups = 1)
    @Warmup(iterations = 1)
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    fun MethodLoop2(bh: Blackhole, state: BenchmarkState) {
        for (str in state.testSet)
          bh.consume(replaceSpecialCharsWithLoop2(str, state.startCode, state.endCode, state.wsCode))
    }

    @Fork(value = 1, warmups = 1)
    @Warmup(iterations = 1)
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    fun MethodLoop3(bh: Blackhole, state: BenchmarkState) {
        for (str in state.testSet)
          bh.consume(replaceSpecialCharsWithLoop3(str, state.startCode, state.endCode, state.wsCode))
    }

    @Fork(value = 1, warmups = 1)
    @Warmup(iterations = 1)
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    fun MethodLoop4(bh: Blackhole, state: BenchmarkState) {
        for (str in state.testSet)
          bh.consume(replaceSpecialCharsWithLoop4(str, state.startCode, state.endCode, state.wsCode))
    }

    @Fork(value = 1, warmups = 1)
    @Warmup(iterations = 1)
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    fun MethodLoop5(bh: Blackhole, state: BenchmarkState) {
        for (str in state.testSet)
          bh.consume(replaceSpecialCharsWithLoop5(str, state.startCode, state.endCode, state.wsCode))
    }

    @Fork(value = 1, warmups = 1)
    @Warmup(iterations = 1)
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    fun MethodRegex(bh: Blackhole, state: BenchmarkState) {
        for (str in state.testSet)
          bh.consume(replaceSpecialCharsWithRegex(str, state.unsupported, state.ws))
    }
}
