package tests


data class Node(
	val name: String,
	val children: List<Node> = emptyList()
)
