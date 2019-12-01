package es.iessaladillo.pedrojoya.pr04.data.entity

data class Task (private val id: Long, private val concept: String, private val createdAt: String, private var completed: Boolean, private var completedAt: String) {

    constructor(concept: String, createdAt: String, completed: Boolean, completedAt: String) : this(++autoNumeric, concept, createdAt, completed, completedAt)

    companion object {
        var autoNumeric: Long = 0
    }

    fun getId() : Long {
        return id

    }

    fun getConcept(): String {
        return concept
    }

    fun isCompleted() : Boolean {
        return completed
    }

    fun getCreatedAt(): String {
        return createdAt
    }

    fun getText(): String {
        return if (!completed) {
            createdAt
        } else {
            completedAt
        }
    }

    fun setCompleted(completed: Boolean) {
        this.completed = completed
        if (!completed) {
            completedAt = ""
        }
    }

    fun setCompletedAt(completedAt: String) {
        this.completedAt = completedAt
    }
}