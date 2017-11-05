package net.matlux.utils

import java.util.concurrent.atomic.AtomicReference

import scala.annotation.tailrec

/** Atoms provide a way to manage shared, synchronous, independent state.
  * They are a reference type like var(s) but with a CAS semantic.
  *
  * See, http://clojure.org/atoms
  *
  * @param state initial state
  * @tparam T type of the state
  */

class Atom[T](private val state: AtomicReference[T]) {

  /** de-ref the atom
    *
    * @return the current state */
  def deRef() = state.get()

  /**  Atomically swaps the value of atom to be:
    * (apply f current-value-of-atom).
    *
    * @param f Note that f may be called multiple times, and thus should be free of side effects.
    * @return Returns the value that was swapped in.
    */
  @tailrec final def swap(f: T => T): T = {
    val oldval = state.get()
    val newval = f(oldval)
    if (state.compareAndSet(oldval, newval)) newval else swap(f)
  }
  /*@tailrec final def swap(f: T => T,args : Any*): T = {
    val oldval = state.get()
    val newval = f(oldval, args)
    if (state.compareAndSet(oldval, newval)) newval else swap(f,args)
  }*/


  /** Sets the value of atom to newval without regard for the
    * current value. Returns newval.
    *
    * @param newval New value of the state
    */
  def reset(newval: T) = state.set(newval)

  override def toString() = deRef.toString
  override def equals(that: Any) = that match {
    case that: Atom[T]  => deRef == that.deRef
    case _              => false
  }
}

object Atom {
  def apply[T](state: T) = new Atom[T](new AtomicReference[T](state))
}