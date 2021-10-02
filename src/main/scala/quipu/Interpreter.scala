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
 * Contributor(s):
 *  - David Loscutoff
 *  - cgccuser
 *
 */

package quipu

import collection.mutable.ArrayBuffer

class InterpreterException(message: String) extends Exception(message)

object Interpreter {

  def apply(code: Array[Array[Knot]]) = {
    import Knot.*

    def resolveJump(n: Any): Int = n match {
      case i: BigInt if (0 <= i && i < code.length) => i.intValue
      case _ => throw InterpreterException("No such thread: \"" + n + "\".")
    }

    var pointer = 0
    var halted = code.isEmpty

    while (!halted) { // over threads
      val thread = code(pointer)

      val stack: ArrayBuffer[Any] = thread(0) match {
        case NumberKnot(n) => ArrayBuffer(n)
        case StringKnot(s) => ArrayBuffer(s)
        case k =>
          throw InterpreterException(s"Unexpected kind of knot $k at start")
      }

      var knots: List[Knot] = thread.toList.tail // skips the self knot
      var jumped = false
      var finished = knots.isEmpty

      while (!halted && !finished && !jumped) { // over knots
        knots.head match {
          case ReferenceKnot =>
            val ref = stack.head match {
              case i: BigInt if (0 <= i && i < code.length) =>
                code(i.intValue)(0) match {
                  case NumberKnot(n) => n
                  case StringKnot(s) => s
                  case k =>
                    throw InterpreterException(
                      s"Unexpected kind of knot $k at ${i.intValue}"
                    )
                }
              case _ =>
                throw InterpreterException(
                  "No such thread: \"" + stack(0) + "\"."
                )
            }
            stack(0) = ref
          case NumberKnot(n) => n +=: stack //Prepend n to stack
          case StringKnot(s) => s +=: stack
          case SelfKnot      => stack.last +=: stack
          case CopyKnot      => stack.head +=: stack
          case OperationKnot(fn) =>
            if (stack.size < 2)
              throw InterpreterException(
                "Not enough arguments for operation."
              )
            (stack(1), stack(0)) match {
              case (a: BigInt, b: BigInt) => fn(a, b) +=: stack
              case _ => throw InterpreterException("Type mismatch.")
            }
          case ConditionalJumpKnot(p) =>
            val target = stack.head
            stack.dropInPlace(1)
            stack.head match {
              case i: BigInt =>
                if (p(i)) {
                  jumped = true
                  pointer = resolveJump(target)
                }
              case _ =>
                throw InterpreterException(
                  "Can't apply jump predicate to: " + stack.head
                )
            }
          case JumpKnot =>
            val target = stack(0)
            stack.dropInPlace(1)
            jumped = true
            pointer = resolveJump(target)
          case InKnot =>
            val str = scala.io.StdIn.readLine()
            try {
              BigInt(str) +=: stack
            } catch {
              case e: NumberFormatException => str +=: stack
            }
          case OutKnot  => Console.print(stack.head)
          case HaltKnot => halted = true
        }

        knots = knots.tail
        finished = knots.isEmpty
      }

      if (!jumped && !halted) {
        if (pointer + 1 == code.length) {
          halted = true
        } else {
          pointer += 1
        }
      }

      if (!halted) {
        thread(0) = stack(0) match {
          case i: BigInt => new NumberKnot(i)
          case s: String => new StringKnot(s)
        }
      }
    }
  }
}
