/*
 * Copyright 2013, by Vladimir Kostyukov and Contributors.
 * Modifications copyright 2021, by cgccuser and Contributors.
 *
 * This file is part of Quipu project (https://github.com/vkostyukov/quipu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributor(s): -
 *  - cgccuser
 */

package quipu

object Main {
  def main(args: Array[String]): Unit = {
    try {
      Interpreter(Parser(scala.io.Source.fromFile(args(0))))
    } catch {
      case pe: ParserException => println("Parser error: " + pe.getMessage)
      case ie: InterpreterException =>
        println("Interpreter error: " + ie.getMessage)
      case _: IndexOutOfBoundsException => println("No input file.")
    }
  }
}
