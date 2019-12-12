package net.matlux

import org.specs2._
import org.specs2.specification.dsl
import org.specs2.specification.dsl.GWT



class SocialNetworkServiceBddTest extends Specification  {

//  def is = s2"""
//
// This is a specification to check the 'Hello world' string
//
// The 'Hello world' string should
//   contain 11 characters                                         $e1
//   start with 'Hello'                                            $e2
//   end with 'world'                                              $e3
//                                                                 """
//
//  def e1 = "Hello world" must have size(11)
//  def e2 = "Hello world" must startWith("Hello")
//  def e3 = "Hello world" must endWith("world")

  /*
      Given a social network name Facebook
    And a full Facebook graph
    Return count of people with no connections
   */

  def is = s2"""

 Test a social network name Facebook

 Given a social network name Facebook
   contain 11 characters                                         $e1
   And a full Facebook graph                                     $e2
   Return count of people with no connections                    $e3
                                                                 """

  def e1 = "Hello world" must have size(11)
  def e2 = "Hello world" must startWith("Hello")
  def e3 = "Hello world" must endWith("world")

}
