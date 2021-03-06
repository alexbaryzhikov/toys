package core

import Constraints
import Model
import interfaces.Cook
import model.Constraint
import model.Entry
import model.Relation
import model.Rule
import java.util.*

class CookImpl : Cook {

    private val constraints: Constraints = TreeMap()

    override fun add(constraint: Constraint) {
        constraints[constraint.id] = constraint
    }

    override fun prepare(): Pair<Constraints, Model> {
        constraints.addReciprocalRelations()
        val model = constraints.cookModel()
        return Pair(constraints, model)
    }

    private fun Constraints.addReciprocalRelations() {
        println("> Add reciprocal relations")
        for (constraint in values) {
            for ((i, entry) in constraint.entries.withIndex()) {
                if (entry is Entry.RuleSet) {
                    for (rule in entry.rules) {
                        val reciprocalRelation = Relation(
                            rule.relation.gName,
                            rule.relation.g,
                            rule.relation.fName,
                            rule.relation.f
                        )
                        val reciprocalRule = Rule(reciprocalRelation, constraint.id)
                        val otherConstraint = get(rule.id)
                            ?: throw IllegalStateException("Constraint ${rule.id} not found")
                        val otherEntry = otherConstraint.entries[i]
                        otherConstraint.entries[i] = when (otherEntry) {
                            is Entry.None -> Entry.RuleSet(hashSetOf(reciprocalRule))
                            is Entry.Value -> otherEntry
                            is Entry.RuleSet -> Entry.RuleSet(otherEntry.rules + reciprocalRule)
                        }
                    }
                }
            }
        }
    }

    private fun Constraints.cookModel(): Model {
        println("> Cook model")
        val model: Model = TreeMap()
        for ((id, constraint) in this) {
            val constraintValues =
                ArrayList<HashSet<Int>>(Constraint.ENTRIES_SIZE)
            for (entry in constraint.entries) {
                val entryValues = if (entry is Entry.Value) {
                    hashSetOf(entry.v)
                } else {
                    HashSet(Constraint.defaultVariants)
                }
                constraintValues.add(entryValues)
            }
            model[id] = constraintValues
        }
        return model
    }
}