package com.sainsburys

import java.io.File
import com.github.tototoshi.csv._

/**
  * Class provides CSV data cleaning functionality (for name, phone and e-mail types of data).
  * Assumptions:
  *   1) Phone number should be 11 or 12 digits long
  *   2) If any of the data fields is missing then the row gets rejected
  *   3) Name consists of English (a-z and A-Z) characters
  *   4) In e-mail @ (at) sign can be mistyped as "!", "£", "$", "-" characters
  *   5) In e-mail . (dot) can be mistyped as ","
  *
  *   Author: Andrey Dmitriev
  *   
  */
object DataCleaning extends App {

  /* ************************************************************************************** */
  /* Initialise counters                                                                    */
  /* ************************************************************************************** */
  var numberOfRejectedRows = 0
  var totalNumberOfRows = 0
  var numberOfFixedValues = 0

  /* ************************************************************************************** */
  /* General Email Regex (RFC 5322 Official Standard)                                       */
  /* ************************************************************************************** */
  val emailValidationString = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"

  /* ************************************************************************************** */
  /* Remove extra whitespaces from name values                                              */
  /* ************************************************************************************** */
  val removeExtraWhiteSpaces = (str:String) => str.replaceAll("\\s+"," ")

  /* ************************************************************************************** */
  /* Remove all whitespaces                                                                 */
  /* ************************************************************************************** */
  val removeWhiteSpaces = (str:String) => str.replaceAll("\\s+","")

  /* ************************************************************************************** */
  /* Keep only Latin1 characters                                                            */
  /* ************************************************************************************** */
  val removeComma = (str:String) => str.replaceAll("[^a-zA-Z]"," ")

  /* ************************************************************************************** */
  /* Remove Mr, Mrs, Ms, Miss occurrences from the name field                               */
  /* ************************************************************************************** */
  val removeMr = (str:String) => str.replaceAll("(?i)\\bmr\\b|\\bmrs\\b|\\bms\\b|\\bmiss\\b"," ")

  /* ************************************************************************************** */
  /* Remove (0) from phone number (e.g. 44 (0) 7785519820 )                                 */
  /* ************************************************************************************** */
  val removeZero = (str:String) => str.replaceAll("\\(0\\)"," ")

  /* ************************************************************************************** */
  /* Remove non digits from phone field                                                     */
  /* ************************************************************************************** */
  val removeNonDigit = (str:String) => str.replaceAll("\\D"," ")

  /* ************************************************************************************** */
  /* Check length of the phone number                                                       */
  /* ************************************************************************************** */
  val checkPhoneLength = (str:String) => if(str.length != 11 && str.length != 12) "" else str

  /* ************************************************************************************** */
  /* Replace mistyped characteds instead of @ in e-mail                                     */
  /* ************************************************************************************** */
  val replaceAt = (str:String) => str.replaceAll("[!£$-]","@")

  /* ************************************************************************************** */
  /* Replace mistyped , instead of . in e-mail                                              */
  /* ************************************************************************************** */
  val replaceDot = (str:String) => str.replaceAll(",",".")

  /* ************************************************************************************** */
  /* Remove mailto: prefix from e-mail                                                      */
  /* ************************************************************************************** */
  val removeMailTo = (str:String) => str.replaceAll("(?i)mailto:","")

  /* ************************************************************************************** */
  /* Validate e-mail address according to RFC 5322 (Official Standard)                      */
  /* ************************************************************************************** */
  val validateEmail = (str:String) => if(!str.matches(emailValidationString)) "" else str


  /* ************************************************************************************** */
  /* Group filtering functions by field type                                                */
  /* ************************************************************************************** */
  val nameDataCleaningFuncList = List(removeExtraWhiteSpaces, removeMr, removeComma)
  val phoneDataCleaningFuncList = List(checkPhoneLength, removeWhiteSpaces, removeNonDigit, removeZero)
  val emailDataCleaningFuncList = List(validateEmail, removeWhiteSpaces, replaceAt, replaceDot, removeMailTo)


  /* ************************************************************************************** */
  /* Read in test data                                                                      */
  /* ************************************************************************************** */
  val reader = CSVReader.open(new File("data/sainsburys_test_data.csv"))

  /* ************************************************************************************** */
  /* Write cleaned data into the file                                                       */
  /* ************************************************************************************** */
  val writer = CSVWriter.open(new File("data/sainsburys_test_data_fixed.csv"))

  /* ************************************************************************************** */
  /* Write rejected data into rejects file                                                  */
  /* ************************************************************************************** */
  val writerRejects = CSVWriter.open(new File("data/sainsburys_test_data_rejects.csv"))


  /* ************************************************************************************** */
  /* Iterate through each row, validate, clean and write to the output file or to rejects   */
  /* ************************************************************************************** */
  for (row <- reader) {

    /* ************************************************************************************** */
    /* Apply validation functions to each row of data                                         */
    /* ************************************************************************************** */
    val name = nameDataCleaningFuncList.reduce(_ compose _)(row(0))
    val phone = phoneDataCleaningFuncList.reduce(_ compose _)(row(1))
    val email = emailDataCleaningFuncList.reduce(_ compose _)(row(2))

    /* ************************************************************************************** */
    /* Count number of records in the file                                                    */
    /* ************************************************************************************** */
    totalNumberOfRows=totalNumberOfRows+1

    /* ************************************************************************************** */
    /* Count number of rejected records                                                       */
    /* ************************************************************************************** */
    if(name.isEmpty || phone.isEmpty || email.isEmpty) {
      numberOfRejectedRows=numberOfRejectedRows+1
      writerRejects.writeRow(row)
    }
    else writer.writeRow(List(name, phone, email))

    /* ************************************************************************************** */
    /* Count number of fixes done on the data fields                                          */
    /* ************************************************************************************** */
    if(name != row(0) && !name.isEmpty) numberOfFixedValues=numberOfFixedValues+1
    if(phone != row(1) && !phone.isEmpty) numberOfFixedValues=numberOfFixedValues+1
    if(email != row(2) && !email.isEmpty) numberOfFixedValues=numberOfFixedValues+1
  }

  /* ************************************************************************************** */
  /* Print out metrics                                                                      */
  /* ************************************************************************************** */
  println("\n")
  println("Total number of rows: " + totalNumberOfRows)
  println("Number of rejected: " + numberOfRejectedRows)
  println("Number of fixed values: " + numberOfFixedValues)
  println("\n")

  /* ************************************************************************************** */
  /* Close files                                                                            */
  /* ************************************************************************************** */
  reader.close()
  writer.close()
  writerRejects.close()
}
