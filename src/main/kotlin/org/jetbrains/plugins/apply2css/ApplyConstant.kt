package org.jetbrains.plugins.apply2css

import com.google.gson.Gson


class ApplyConstant {
    companion object {
        // https://tailwind.build/classes
        private val utility = mutableMapOf<String, String>()
        private var map: MutableMap<Char, MutableMap<String, String>> = mutableMapOf()

        init {
            val inputStream = this::class.java.classLoader.getResourceAsStream("intentionDescriptions/ApplyToCSSIntentionAction/utility.json")
            if (inputStream != null) {
                val fileContent = inputStream.bufferedReader().use { it.readText() }
                val gson = Gson()
                val jsonData = gson.fromJson(fileContent, Map::class.java)
                jsonData.keys.forEach { key ->
                    val strKey = key.toString()
                    val strValue = jsonData[key].toString()
                    utility[strKey] = strValue
                    val mapGroup = map.computeIfAbsent(strKey[0]) { mutableMapOf() }
                    mapGroup[strKey] = strValue
                }
            } else {
                println("Resource file not found!")
            }
        }

        fun filterByKeyword(key: String): Map<String, String> {
            return utility.filter { item -> item.key.contains(key) }
        }

        fun filterByFirstChar(char: Char): MutableMap<String, String>? {
            return map[char]
        }
    }
}
