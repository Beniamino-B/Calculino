package com.calculino.app.domain.engineering

data class ComplexityEntry(
    val name: String,
    val best: String,
    val average: String,
    val worst: String,
    val space: String,
)

object ComplexityReference {
    val entries: List<ComplexityEntry> = listOf(
        ComplexityEntry("Búsqueda lineal", "O(1)", "O(n)", "O(n)", "O(1)"),
        ComplexityEntry("Búsqueda binaria", "O(1)", "O(log n)", "O(log n)", "O(1)"),
        ComplexityEntry("Bubble Sort", "O(n)", "O(n²)", "O(n²)", "O(1)"),
        ComplexityEntry("Insertion Sort", "O(n)", "O(n²)", "O(n²)", "O(1)"),
        ComplexityEntry("Merge Sort", "O(n log n)", "O(n log n)", "O(n log n)", "O(n)"),
        ComplexityEntry("QuickSort", "O(n log n)", "O(n log n)", "O(n²)", "O(log n)"),
        ComplexityEntry("Heap Sort", "O(n log n)", "O(n log n)", "O(n log n)", "O(1)"),
        ComplexityEntry("BFS", "O(V + E)", "O(V + E)", "O(V + E)", "O(V)"),
        ComplexityEntry("DFS", "O(V + E)", "O(V + E)", "O(V + E)", "O(V)"),
        ComplexityEntry("Dijkstra", "O((V + E) log V)", "O((V + E) log V)", "O((V + E) log V)", "O(V)"),
        ComplexityEntry("Fibonacci recursivo", "O(2ⁿ)", "O(2ⁿ)", "O(2ⁿ)", "O(n)"),
        ComplexityEntry("Programación dinámica Fibonacci", "O(n)", "O(n)", "O(n)", "O(1)"),
    )
}
