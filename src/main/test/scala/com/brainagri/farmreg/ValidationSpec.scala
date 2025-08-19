package com.brainagri.farmreg

import org.scalatest.funsuite.AnyFunSuite
import com.brainagri.farmreg.validation.Validators

class ValidationSpec extends AnyFunSuite {
  test("CPF inválido repetido") {
    assert(!Validators.isValidCPF("111.111.111-11"))
  }
  test("CNPJ inválido repetido") {
    assert(!Validators.isValidCNPJ("11.111.111/1111-11"))
  }
}
