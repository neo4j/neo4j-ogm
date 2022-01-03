/*
 * Copyright (c) 2002-2022 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.kotlin

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.neo4j.ogm.domain.delegation.KotlinAImpl
import org.neo4j.ogm.metadata.MetaData

/**
 * @author Michael J. Simons
 * @soundtrack Die Toten Hosen - Kauf MICH!
 */
class KotlinMetaDataTest {

    @Test // GH-685
    fun `Kotlin's "implementation by delegation" should yield same result as Java equivalent`() {
        val metaData = MetaData("org.neo4j.ogm.domain.gh685", "org.neo4j.ogm.domain.delegation")

        val javaClassInfo = metaData.classInfo("JavaAImpl")
        assertThat(javaClassInfo.staticLabels()).containsExactlyInAnyOrder("Base", "A")
        val javaFields = javaClassInfo.fieldsInfo().fields().map { it.name }
        assertThat(javaFields).containsExactlyInAnyOrder("id", "ownAttr", "baseName")
        assertThat(javaClassInfo.postLoadMethodOrNull()).isNotNull

        val kotlinClassInfo = metaData.classInfo("KotlinAImpl")
        assertThat(kotlinClassInfo.staticLabels()).containsExactlyInAnyOrderElementsOf(javaClassInfo.staticLabels())
        val kotlinFields = kotlinClassInfo.fieldsInfo().fields().map { it.name }
        assertThat(kotlinFields).containsExactlyInAnyOrderElementsOf(javaFields)
        assertThat(kotlinClassInfo.postLoadMethodOrNull()).isNotNull

        val aKotlinClass = KotlinAImpl()
        val baseNameField = kotlinClassInfo.getFieldInfo("baseName")

        baseNameField.write(aKotlinClass, "something")
        assertThat(aKotlinClass.baseName).isEqualTo("something")

        aKotlinClass.baseName = "somethingElse"
        assertThat(baseNameField.read(aKotlinClass)).isEqualTo("somethingElse")
    }

    @Test // GH-696
    fun `Type descriptor of Kotlin collections containing Java classes should be resolved`() {
        val metaData = MetaData("org.neo4j.ogm.domain.gh696")
        val javaZoo = metaData.classInfo("ZooJava")
        val kotlinZoo = metaData.classInfo("ZooKotlin")

        assertThat(javaZoo).isNotNull
        assertThat(javaZoo.getFieldInfo("animals").typeDescriptor).isEqualTo("org.neo4j.ogm.domain.gh696.Animal")
        assertThat(kotlinZoo).isNotNull
        assertThat(kotlinZoo.getFieldInfo("animals").typeDescriptor).isEqualTo("org.neo4j.ogm.domain.gh696.Animal")
        assertThat(kotlinZoo.getFieldInfo("zookeepers").typeDescriptor).isEqualTo("org.neo4j.ogm.domain.gh696.Zookeeper")
    }

    @Test // GH-822
    fun `OGM should detect the inlines class underlying value type`() {
        val metaData = MetaData("org.neo4j.ogm.domain.gh822")
        val userClassInfo = metaData.classInfo("User")

        assertThat(userClassInfo).isNotNull
        assertThat(userClassInfo.getFieldInfo("userId").typeDescriptor).isEqualTo("java.lang.String")
    }
}
