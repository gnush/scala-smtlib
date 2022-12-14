package com.github.gnush.smt.smtlib

import org.scalatest.funsuite.AnyFunSuite
import syntax.{Apply, Binary, ComposedIdentifier, Decimal, Exists, Forall, Hexadecimal, IdentifierAs, Keyword, Let, Numeral, QuotedSymbol, SExpr, SExprs, SMTLibString, SimpleSymbol, SortedVar, Sorts, VarBinding}
import theory.BoolPredefined._

class TestFormatting extends AnyFunSuite{
  // TODO: successfully creating these objects is already a test (requires clause)
  //  add a test prior to verify that no exception gets thrown
  // TODO: maybe update the other tests s.t. all used variables are local to the test

  private val num1 = Numeral(0)
  private val num2 = Numeral(1256432)
  private val dec1 = Decimal(123.456)
  private val dec2 = Decimal(123.0123)
  private val hex1 = Hexadecimal(0x0a1b54ff)
  private val hex2 = Hexadecimal("0a1b54ff")
  private val hex3 = Hexadecimal(0x1a2b)
  private val hex4 = Hexadecimal("1a2b")
  private val hex5 = Hexadecimal(0xabcdef)
  private val bin1 = Binary(Integer.parseInt("00111100", 2))
  private val bin2 = Binary("00111100")
  private val bin3 = Binary(0xff)
  private val bin4 = Binary("11111111")
  private val str1 = SMTLibString("foo")
  private val str2 = SMTLibString("bar")
  private val symbol1 = SimpleSymbol("<=")
  private val symbol2 = SimpleSymbol("plus")
  private val symbol3 = SimpleSymbol("*$s&6")
  private val symbol4 = SimpleSymbol(".8")
  private val symbol5 = SimpleSymbol("+34-32")
  private val symbol6 = QuotedSymbol("this is a quoted symbol")
  private val symbol7 = QuotedSymbol("so is\nthis  one")
  private val symbol8 = QuotedSymbol(" \" can occur too")
  private val symbol9 = QuotedSymbol("")
  private val symbol0 = QuotedSymbol("af klj ^*0 asfe2 (&*)&(#^$>> >?\" ’]]984")
  private val keyword1 = Keyword("foo-bar")
  private val keyword2 = Keyword("<=")
  private val keyword3 = Keyword("->")

  test("Lexicon") {
    assert(num1.format == "0")
    assert(num2.format == "1256432")
    assert(dec1.format == "123.456")
    assert(dec2.format == "123.0123")
    assert(hex1.format == "#xA1B54FF")
    assert(hex2.format == "#x0A1B54FF")
    assert(hex3.format == "#x1A2B")
    assert(hex4.format == "#x1A2B")
    assert(hex5.format == "#xABCDEF")
    assert(bin1.format == "#b111100")
    assert(bin2.format == "#b00111100")
    assert(bin3.format == "#b11111111")
    assert(bin4.format == "#b11111111")
    assert(str1.format == "\"foo\"")
    assert(str2.format == "\"bar\"")
    assert(symbol1.format == "<=")
    assert(symbol2.format == "plus")
    assert(symbol3.format == "*$s&6")
    assert(symbol4.format == ".8")
    assert(symbol5.format == "+34-32")
    assert(symbol6.format == "|this is a quoted symbol|")
    assert(symbol7.format == "|so is\nthis  one|")
    assert(symbol8.format == "| \" can occur too|")
    assert(symbol9.format == "||")
    assert(symbol0.format == "|af klj ^*0 asfe2 (&*)&(#^$>> >?\" ’]]984|")
    assert(keyword1.format == ":foo-bar")
    assert(keyword2.format == ":<=")
    assert(keyword3.format == ":->")

    assertThrows[IllegalArgumentException]{
      Hexadecimal("#xFF")
    }

    assertThrows[IllegalArgumentException]{
      Hexadecimal("0xFF")
    }

    assertThrows[IllegalArgumentException] {
      Binary("123456")
    }

    assertThrows[IllegalArgumentException] {
      Binary("0b111")
    }

    assertThrows[IllegalArgumentException] {
      SimpleSymbol("")
    }

    assertThrows[IllegalArgumentException] {
      SimpleSymbol("0asdf")
    }

    assertThrows[IllegalArgumentException] {
      SimpleSymbol("a0b1(")
    }
  }

  test("SExpr") {
    val sExpr1: SExpr = Numeral(123)
    val sExpr2 = SExprs(Seq(num1, dec1, hex1, bin1, str1, symbol1, keyword1))

    assert(sExpr1.format == "123")
    assert(sExpr2.format == "(0 123.456 #xA1B54FF #b111100 \"foo\" <= :foo-bar)")
  }

  test("Identifier") {
    val identifier1 = ComposedIdentifier(SimpleSymbol("vector-add"), Seq(Numeral(4), Numeral(5)))
    val identifier2 = ComposedIdentifier(SimpleSymbol("move"), Seq(SimpleSymbol("down")))
    val identifier3 = ComposedIdentifier(SimpleSymbol("move-length"), Seq(SimpleSymbol("left"), Numeral(2)))

    assert(identifier1.format == "(_ vector-add 4 5)")
    assert(identifier2.format == "(_ move down)")
    assert(identifier3.format == "(_ move-length left 2)")
  }

  test("Sort") {
    assert(Bool.format == "Bool")
    assert(SimpleSymbol("Int").format == "Int")
    assert(SimpleSymbol("String").format == "String")

    val array = Sorts(SimpleSymbol("Array"), Seq(SimpleSymbol("Int"), SimpleSymbol("Real")))
    assert(array.format == "(Array Int Real)")

    val listArray = Sorts(SimpleSymbol("List"), Seq(array))
    assert(listArray.format == "(List (Array Int Real))")

    val bitVec = ComposedIdentifier(SimpleSymbol("BitVec"), Seq(Numeral(3)))
    assert(bitVec.format == "(_ BitVec 3)")

    val fixedList = Sorts(ComposedIdentifier(SimpleSymbol("FixedSizeList"), Seq(Numeral(4))), Seq(SimpleSymbol("Real")))
    assert(fixedList.format == "((_ FixedSizeList 4) Real)")

    val set = Sorts(SimpleSymbol("Set"), Seq(ComposedIdentifier(SimpleSymbol("BitVec"), Seq(Numeral(3)))))
    assert(set.format == "(Set (_ BitVec 3))")
  }

  test("Term.QualifiedIdentifier") {
    assert(SimpleSymbol("x").format == "x")
    assert(IdentifierAs(SimpleSymbol("x"), Bool).format == "(as x Bool)")
  }

  test("Term.Application") {
    assert(Apply(SimpleSymbol("append"), Seq(SimpleSymbol("x"), SimpleSymbol("y"))).format == "(append x y)")
  }

  private val binding1 = VarBinding(SimpleSymbol("h"), Apply(SimpleSymbol("head"), Seq(SimpleSymbol("x"))))
  private val binding2 = VarBinding(SimpleSymbol("t"), Apply(SimpleSymbol("tail"), Seq(SimpleSymbol("x"))))
  private val let = Let(Seq(binding1, binding2),
    Apply(SimpleSymbol("insert"),
      Seq(SimpleSymbol("h"),
        Apply(SimpleSymbol("append"),
          Seq(SimpleSymbol("t"), SimpleSymbol("y"))))))

  test("Term.Let") {
    assert(binding1.format == "(h (head x))")
    assert(binding2.format == "(t (tail x))")
    assert(let.format == "(let ((h (head x)) (t (tail x))) (insert h (append t y)))")
  }

  private val var1 = SortedVar(SimpleSymbol("x"), Sorts(SimpleSymbol("List"), Seq(SimpleSymbol("Int"))))
  private val var2 = SortedVar(SimpleSymbol("y"), Sorts(SimpleSymbol("List"), Seq(SimpleSymbol("Int"))))

  test("Term.Forall") {
    assert(var1.format == "(x (List Int))")
    assert(var2.format == "(y (List Int))")

    val forall = Forall(Seq(var1, var2), let)
    assert(forall.format == "(forall ((x (List Int)) (y (List Int))) (let ((h (head x)) (t (tail x))) (insert h (append t y))))")
  }

  test("Term.Exists") {
    val exists = Exists(Seq(var1, var2), let)
    assert(exists.format == "(exists ((x (List Int)) (y (List Int))) (let ((h (head x)) (t (tail x))) (insert h (append t y))))")
  }

  test("Term nested quantifiers") {
    assert(var1.format == "(x (List Int))")
    assert(var2.format == "(y (List Int))")
    assert(binding1.format == "(h (head x))")
    assert(binding2.format == "(t (tail x))")

    val term = Forall(Seq(var1),
      Exists(Seq(var2),
        Let(
          Seq(binding1, binding2),
          Eq(
            Apply(SimpleSymbol("insert"), Seq(SimpleSymbol("h"), SimpleSymbol("t"))),
            SimpleSymbol("y")))))

    assert(term.format == "(forall ((x (List Int))) (exists ((y (List Int))) (let ((h (head x)) (t (tail x))) (= (insert h t) y))))")
  }

  test("Term.Sugar") {
    assert(True.format == "true")
    assert(False.format == "false")
    assert(Not(True).format == "(not true)")
    assert(Implies(False, True).format == "(=> false true)")
    assert(And(Not(False), True).format == "(and (not false) true)")
    assert(Xor(Distinct(SimpleSymbol("x"), SimpleSymbol("y")), Distinct(SimpleSymbol("x"), SimpleSymbol("z"))).format == "(xor (distinct x y) (distinct x z))")
    assert(Ite(Not(False), Or(True, False), Eq(SimpleSymbol("y"), SimpleSymbol("z"))).format == "(ite (not false) (or true false) (= y z))")
  }
}
