import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicReferenceArray

/**
 * @author TODO: Last Name, First Name
 */
open class TreiberStackWithElimination<E> : Stack<E> {
    private val stack = TreiberStack<E>()

    // TODO: Try to optimize concurrent push and pop operations,
    // TODO: synchronizing them in an `eliminationArray` cell.
    private val eliminationArray = AtomicReferenceArray<Any?>(ELIMINATION_ARRAY_SIZE)

    override fun push(element: E) {
        if (tryPushElimination(element)) return
        stack.push(element)
    }

    protected open fun tryPushElimination(element: E): Boolean {
        //TODO("Implement me!")
        // TODO: Choose a random cell in `eliminationArray`
        // TODO: and try to install the element there.
        // TODO: Wait `ELIMINATION_WAIT_CYCLES` loop cycles
        // TODO: in hope that a concurrent `pop()` grabs the
        // TODO: element. If so, clean the cell and finish,
        // TODO: returning `true`. Otherwise, move the cell
        // TODO: to the empty state and return `false`.
        var iterations = ELIMINATION_WAIT_CYCLES
        val randomCellIndex = randomCellIndex()
        var selectedIndex: Int? = null
        if (eliminationArray.compareAndSet(randomCellIndex, CELL_STATE_EMPTY, element)) {
                selectedIndex = randomCellIndex
        }

        selectedIndex ?: return false
        while (iterations > 0) {
            iterations--
            if (eliminationArray.compareAndSet(selectedIndex, CELL_STATE_RETRIEVED, CELL_STATE_EMPTY)) {
                return true
            }
        }
        val successCasEmpty = eliminationArray.compareAndSet(selectedIndex, element, CELL_STATE_EMPTY)
        if (!successCasEmpty) {
            return eliminationArray.compareAndSet(selectedIndex, CELL_STATE_RETRIEVED, CELL_STATE_EMPTY)
        }
        return false
    }

    override fun pop(): E? = tryPopElimination() ?: stack.pop()

    private fun tryPopElimination(): E? {
        //TODO("Implement me!")
        // TODO: Choose a random cell in `eliminationArray`
        // TODO: and try to retrieve an element from there.
        // TODO: On success, return the element.
        // TODO: Otherwise, if the cell is empty, return `null`.
        val randomCellIndex = randomCellIndex()
        val elementVal = eliminationArray.get(randomCellIndex)
        if (elementVal === CELL_STATE_EMPTY || elementVal === CELL_STATE_RETRIEVED) return null
        if (eliminationArray.compareAndSet(randomCellIndex, elementVal, CELL_STATE_RETRIEVED)) {
            return elementVal as E
        }
        return null
    }

    private fun randomCellIndex(): Int =
        ThreadLocalRandom.current().nextInt(eliminationArray.length())

    companion object {
        private const val ELIMINATION_ARRAY_SIZE = 2 // Do not change!
        private const val ELIMINATION_WAIT_CYCLES = 1 // Do not change!

        // Initially, all cells are in EMPTY state.
        private val CELL_STATE_EMPTY = null

        // `tryPopElimination()` moves the cell state
        // to `RETRIEVED` if the cell contains element.
        private val CELL_STATE_RETRIEVED = Any()
    }
}
