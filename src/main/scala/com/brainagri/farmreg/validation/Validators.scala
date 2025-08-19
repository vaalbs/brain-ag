package com.brainagri.farmreg.validation

object Validators {
  private def onlyDigits(s: String) = s.filter(_.isDigit)

  def isValidCPF(cpfRaw: String): Boolean = {
    val cpf = onlyDigits(cpfRaw)
    if (cpf.length != 11 || cpf.distinct.length == 1) return false

    def dvCalc(nums: Seq[Int], factorStart: Int): Int = {
      val sum = nums.zipWithIndex.map { case (n, i) => n * (factorStart - i) }.sum
      val rest = sum % 11
      if (rest < 2) 0 else 11 - rest
    }

    val digits = cpf.map(_.asDigit)
    val dv1 = dvCalc(digits.take(9), 10)
    val dv2 = dvCalc(digits.take(10), 11)
    digits(9) == dv1 && digits(10) == dv2
  }

  def isValidCNPJ(cnpjRaw: String): Boolean = {
    val cnpj = onlyDigits(cnpjRaw)
    if (cnpj.length != 14 || cnpj.distinct.length == 1) return false

    def dvCalc(nums: Seq[Int], factors: Seq[Int]): Int = {
      val sum = nums.zip(factors).map { case (n, f) => n * f }.sum
      val rest = sum % 11
      if (rest < 2) 0 else 11 - rest
    }

    val digits = cnpj.map(_.asDigit)
    val f1 = Seq(5,4,3,2,9,8,7,6,5,4,3,2)
    val f2 = Seq(6,5,4,3,2,9,8,7,6,5,4,3,2)

    val dv1 = dvCalc(digits.take(12), f1)
    val dv2 = dvCalc(digits.take(13), f2)

    digits(12) == dv1 && digits(13) == dv2
  }
}
