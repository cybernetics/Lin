package com.serchinastico.lin.detectors

import com.serchinastico.lin.test.LintTest
import com.serchinastico.lin.test.LintTest.Expectation.NoErrors
import com.serchinastico.lin.test.LintTest.Expectation.SomeError
import org.junit.Test

class OnlyConstantsInTypeDetectorTest : LintTest {

    override val issue = OnlyConstantsInTypeOrFileDetector.issue

    @Test
    fun inJavaClass_whenClassHasMethods_detectsNoErrors() {
        expect(
            """
                |package foo;
                |
                |class TestClass {
                |   public static final String str = "";
                |
                |   public void main(String[] args) {}
                |}
            """.inJava
        ) toHave NoErrors
    }

    @Test
    fun inJavaClass_whenClassHasNonStaticFields_detectsNoErrors() {
        expect(
            """
                |package foo;
                |
                |class TestClass {
                |   public static final String str = "";
                |   private String nonStaticStr;
                |}
            """.inJava
        ) toHave NoErrors
    }

    @Test
    fun inJavaClass_whenClassHasNoFieldsNorMethods_detectsError() {
        expect(
            """
                |package foo;
                |
                |class TestClass {
                |   public static final String str;
                |}
            """.inJava
        ) toHave SomeError("src/foo/TestClass.java")
    }

    @Test
    fun inKotlinClass_whenClassHasMethods_detectsNoErrors() {
        expect(
            """
                |package foo
                |
                |class TestClass {
                |
                |   companion object {
                |     const val str: String = ""
                |   }
                |
                |   public fun main(args: Array<String>) {}
                |}
            """.inKotlin
        ) toHave NoErrors
    }

    @Test
    fun inKotlinClass_whenClassHasNonStaticFields_detectsNoErrors() {
        expect(
            """
                |package foo
                |
                |class TestClass {
                |
                |   companion object {
                |     const val str: String = ""
                |   }
                |
                |   val anotherStr: String = ""
                |}
            """.inKotlin
        ) toHave NoErrors
    }

    @Test
    fun inKotlinClass_whenClassHasNoFieldsNorMethods_detectsErrors() {
        expect(
            """
                |package foo
                |
                |class TestClass {
                |   companion object {
                |     const val str: String = ""
                |   }
                |}
            """.inKotlin
        ) toHave SomeError("src/foo/TestClass.kt")
    }

    @Test
    fun inKotlinObject_whenItHasNoFieldsNorMethods_detectsErrors() {
        expect(
            """
                |package foo
                |
                |object TestClass {
                |   const val str: String = ""
                |}
            """.inKotlin
        ) toHave SomeError("src/foo/TestClass.kt")
    }

    @Test
    fun inKotlinSealedClass_whenItHasNoFields_detectsNoErrors() {
        expect(
            """
                |package foo
                |
                |sealed class ActionStatus<out T> {
                |    class Ready<out T> : ActionStatus<T>()
                |    class OnGoing<out T> : ActionStatus<T>()
                |    class Finished<out T>(val value: T) : ActionStatus<T>()
                |}
            """.inKotlin
        ) toHave NoErrors
    }

    @Test
    fun inKotlinInterface_whenItHasNoFields_detectsNoErrors() {
        expect(
            """
                |package foo

                |import okhttp3.RequestBody
                |import retrofit2.Call
                |import retrofit2.http.Body
                |import retrofit2.http.GET
                |import retrofit2.http.PUT
                |import retrofit2.http.Path
                |import retrofit2.http.Query
                |import retrofit2.http.Url
                |
                |interface SomeRetrofitApi {
                |    @GET("some/url")
                |    fun someApiCall(
                |        @Query("param1") param1: String,
                |        @Query("param2") param2: String
                |    ): Call<SomeResponse>
                |}
                |
                |typealias SomeResponse = Map<String, Any?>
            """.inKotlin
        ) toHave NoErrors
    }

    @Test
    fun inKotlinDataClass_whenItHasNoFields_detectsNoErrors() {
        expect(
            """
                |package foo
                |
                |import org.joda.time.LocalDate
                |
                |typealias SomeMap = Map<LocalDate, List<Answer>>
                |
                |data class SomeDataClass(
                |    val someProperty: String,
                |    val someOtherProperty: String
                |)
                |
                |data class OtherDataClass(
                |    val someProperty: String,
                |    val someOtherProperty: String
                |)
                |
                |data class YetAnotherDataClass(
                |    val someProperty: String,
                |    private val someOtherProperty: String
                |) {
                |    val someNonConstructorProperty: String = ""
                |}
            """.inKotlin
        ) toHave NoErrors
    }

    @Test
    fun inKotlinEnum_whenItHasNoFields_detectsNoErrors() {
        expect(
            """
                |enum class LegalNoticeStatus {
                |   NONE, INSTRUCTIONS_SELECTED
                |}
            """.inKotlin
        ) toHave NoErrors
    }

    @Test
    fun inKotlinFile_whenThereIsAGlobalConstant_detectsNoErrors() {
        /*
         * UAST interprets global functions and properties are part of a UClass that is the file.
         * As long as there are other things in the file we are considering it ok.
         */
        expect(
            """
                |package foo
                |
                |import android.arch.persistence.room.Database
                |import android.arch.persistence.room.RoomDatabase
                |import android.arch.persistence.room.TypeConverters
                |import foo.Converters
                |import foo.SomeDao
                |import foo.SomeEntity
                |
                |const val APP_DATABASE_VERSION = 1
                |fun foo() {}
                |
                |@Database(entities = [
                |    SomeEntity::class],
                |    version = APP_DATABASE_VERSION)
                |@TypeConverters(Converters::class)
                |abstract class AppDatabase : RoomDatabase() {
                |    abstract fun someDao(): SomeDao
                |}
            """.inKotlin
        ) toHave NoErrors
    }

    @Test
    fun inKotlinFile_whenThereIsOnlyAGlobalConstant_detectsError() {
        /*
         * UAST interprets global functions and properties are part of a UClass that is the file.
         * If the constant is the only thing defined in this "type" then we raise an error.
         */
        expect(
            """
                |package foo
                |
                |const val APP_DATABASE_VERSION = 1
            """.inKotlin
        ) toHave SomeError("src/foo/TestClass.kt")
    }
}