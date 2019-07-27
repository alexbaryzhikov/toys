package core

import Constraints
import Model
import interfaces.HoradricCube
import interfaces.Merger
import interfaces.Simplifier
import results.HoradricResult

class SimplifierImpl: Simplifier {

    private lateinit var horadricCube: HoradricCube
    private lateinit var merger: Merger

    fun inject(horadricCube: HoradricCube, merger: Merger) {
        this.horadricCube = horadricCube
        this.merger = merger
    }

    override fun simplify(constraints: Constraints, model: Model): HoradricResult {
        println("> Simplify")
        var result: HoradricResult
        do {
            result = horadricCube.transmute(constraints, model)
            if (result is HoradricResult.Match) {
                merger.mergeConstraints(constraints, result.idA, result.idB)
                merger.mergeModel(model, result.idA, result.idB)
            }
        } while (result is HoradricResult.Match)
        return result
    }
}
