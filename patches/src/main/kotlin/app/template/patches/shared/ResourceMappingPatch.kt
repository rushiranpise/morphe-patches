/*
 * Copyright 2025 Morphe.
 * https://github.com/MorpheApp/morphe-patches-library
 *
 * Original code hard forked from:
 * https://github.com/ReVanced/revanced-patches/commit/724e6d61b2ecd868c1a9a37d465a688e83a74799
 *
 * File-Specific License Notice (GPLv3 Section 7 Terms)
 *
 * This file is part of the Morphe project and is licensed under
 * the GNU General Public License version 3 (GPLv3), with the Additional
 * Terms under Section 7 described in the LICENSE file.
 *
 * https://www.gnu.org/licenses/gpl-3.0.html
 *
 * Section 7b: Notice Preservation
 * -------------------------------
 * This entire comment block must be preserved in all copies,
 * distributions, and derivative works of this file, in both
 * original and modified source forms.
 *
 * Portions of this software are provided "AS IS" by the Morphe software project.
 * Any express or implied warranties, including the implied warranties of
 * merchantability and fitness for a particular purpose, are disclaimed.
 */

package app.template.patches.shared

import app.morphe.patcher.InstructionLocation
import app.morphe.patcher.LiteralFilter
import app.morphe.patcher.OpcodesFilter
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.resourcePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.WideLiteralInstruction
import org.w3c.dom.Element

enum class ResourceType(val value: String) {
    ANIM("anim"),
    ANIMATOR("animator"),
    ARRAY("array"),
    ATTR("attr"),
    BOOL("bool"),
    COLOR("color"),
    DIMEN("dimen"),
    DRAWABLE("drawable"),
    FONT("font"),
    FRACTION("fraction"),
    ID("id"),
    INTEGER("integer"),
    INTERPOLATOR("interpolator"),
    LAYOUT("layout"),
    MENU("menu"),
    MIPMAP("mipmap"),
    NAVIGATION("navigation"),
    PLURALS("plurals"),
    RAW("raw"),
    STRING("string"),
    STYLE("style"),
    STYLEABLE("styleable"),
    TRANSITION("transition"),
    VALUES("values"),
    XML("xml");

    companion object {
        private val VALUE_MAP: Map<String, ResourceType> = entries.associateBy { it.value }

        fun fromValue(value: String) = VALUE_MAP[value]
            ?: throw IllegalArgumentException("Unknown resource type: $value")
    }
}

data class ResourceElement(val type: ResourceType, val name: String, val id: Long)

private lateinit var resourceMappings: MutableMap<String, ResourceElement>

private fun setResourceId(type: ResourceType, name: String, id: Long) {
    resourceMappings[type.value + name] = ResourceElement(type, name, id)
}

/**
 * @return A resource id of the given resource type and name.
 * @throws PatchException if the resource is not found.
 */
fun getResourceId(type: ResourceType, name: String) = resourceMappings[type.value + name]?.id
    ?: throw PatchException("Could not find resource type: $type name: $name")

/**
 * @return If the resource exists.
 */
fun hasResourceId(type: ResourceType, name: String) = resourceMappings[type.value + name] != null

class ResourceLiteralFilter(
    type: ResourceType,
    name: String,
    exceptionIfResourceNotFound: Boolean = true,
    location : InstructionLocation
) : OpcodesFilter(null as List<Opcode>?, location) {

    private val literalValue: Long? by lazy {
        if (exceptionIfResourceNotFound || hasResourceId(type, name)) {
            getResourceId(type, name)
        } else {
            null
        }
    }

    override fun matches(
        enclosingMethod: Method,
        instruction: Instruction
    ): Boolean {
        if (!super.matches(enclosingMethod, instruction)) {
            return false
        }

        if (instruction !is WideLiteralInstruction) return false

        if (literalValue == null) return false

        return instruction.wideLiteral == literalValue
    }
}

/**
 * Identical to [LiteralFilter] except uses a decoded resource literal value.
 *
 * Any patch with fingerprints of this filter must
 * also declare [resourceMappingPatch] as a dependency.
 *
 * @param exceptionIfResourceNotFound If false and the resource does not exist,
 *   then this filter effectively never matches anything. This should only be used
 *   with [app.morphe.patcher.anyInstruction] where one of the resource filters
 *   may not exist in all app versions.
 */
@Suppress("unused")
fun resourceLiteral(
    type: ResourceType,
    name: String,
    exceptionIfResourceNotFound: Boolean = true,
    location : InstructionLocation = InstructionLocation.MatchAfterAnywhere()
) = ResourceLiteralFilter(type, name, exceptionIfResourceNotFound, location)


val resourceMappingPatch = resourcePatch(
    default = false
) {
    execute {
        // Use a stream of the file, since no modifications are done
        // and using a File parameter causes the file to be re-written when closed.
        document(get("res/values/public.xml").inputStream()).use { document ->
            val resources = document.documentElement.childNodes
            val resourcesLength = resources.length
            resourceMappings = HashMap(2 * resourcesLength)

            for (i in 0 until resourcesLength) {
                val node = resources.item(i) as? Element ?: continue
                if (node.nodeName != "public") continue

                val nameAttribute = node.getAttribute("name")
                if (nameAttribute.startsWith("APKTOOL")) continue

                val typeAttribute = node.getAttribute("type")
                val id = node.getAttribute("id").substring(2).toLong(16)

                setResourceId(ResourceType.fromValue(typeAttribute), nameAttribute, id)
            }
        }
    }

    finalize {
        resourceMappings.clear()
    }
}
