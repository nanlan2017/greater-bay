package com.jheng.bay.config.mybatis

import com.jheng.bay.CorePackageClass
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.io.Resources
import org.apache.ibatis.session.SqlSessionFactory
import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import org.mybatis.spring.SqlSessionFactoryBean
import org.mybatis.spring.annotation.MapperScan
import org.mybatis.spring.boot.autoconfigure.MybatisProperties
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.core.type.classreading.CachingMetadataReaderFactory
import org.springframework.core.type.classreading.MetadataReaderFactory
import org.springframework.util.ClassUtils
import org.springframework.util.StringUtils
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.*
import javax.sql.DataSource

/**
 * see also: org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration
 */
// somehow we need this MapperScan to make mybatis work as our expected
@MapperScan(
        basePackageClasses = [CorePackageClass::class],
        annotationClass = Mapper::class
)
//@Configuration
class MybatisConfig {

    @Autowired
    private lateinit var mybatisProperties: MybatisProperties

    companion object {
        private val logger = LoggerFactory.getLogger(MybatisConfig::class.java)
    }

    /**
     * this handler mainly change the behavior when column value is an empty string.
     * that is, we return a null value instead of throw a "no enum constant" error when empty string is encountered
     *
     * see org.apache.ibatis.type.EnumTypeHandler
     */
    class CustomEnumTypeHandler<E : Enum<E>>(private val type: Class<E>) : BaseTypeHandler<E>() {

        override fun getNullableResult(
                rs: ResultSet,
                columnName: String
        ): E? {
            val s = rs.getString(columnName)
            return if (s.isNullOrEmpty()) null else java.lang.Enum.valueOf(type, s)
        }

        override fun getNullableResult(
                rs: ResultSet,
                columnIndex: Int
        ): E? {
            val s = rs.getString(columnIndex)
            return if (s.isNullOrEmpty()) null else java.lang.Enum.valueOf(type, s)
        }

        override fun getNullableResult(
                cs: CallableStatement,
                columnIndex: Int
        ): E? {
            val s = cs.getString(columnIndex)
            return if (s.isNullOrEmpty()) null else java.lang.Enum.valueOf(type, s)
        }

        override fun setNonNullParameter(
                ps: PreparedStatement,
                i: Int,
                parameter: E,
                jdbcType: JdbcType?
        ) {
            if (jdbcType == null) {
                ps.setString(i, parameter.name)
            } else {
                ps.setObject(i, parameter.name, jdbcType.TYPE_CODE) // see r3589
            }
        }
    }

    @Bean
    fun sqlSessionFactory(dataSource: DataSource): SqlSessionFactory? {
        val factoryBean = SqlSessionFactoryBean()
        factoryBean.vfs = SpringBootVFS::class.java
        factoryBean.setDataSource(dataSource)
        factoryBean.setMapperLocations(*PathMatchingResourcePatternResolver().getResources(
                "classpath:mybatis/*.xml"))
        factoryBean.setTypeHandlersPackage("com.jheng.bay.features.**,com.jheng.bay.config.mybatis.**")

        // wild card typeAliasesPackage, typeHandler
        // https://github.com/mybatis/spring-boot-starter/issues/314
        // https://github.com/mybatis/spring/pull/359
//        factoryBean.setTypeAliasesPackage("com.jheng.bay.features.**.model,com.jheng.bay.features.**.pojo")

        val classes = filterTypeAliasesClasses(scanClasses(
                packagePatterns = "com.jheng.bay.features.**.model,com.jheng.bay.features.**.pojo,com.jheng.bay.core.pojo"
        ))

        val configuration = mybatisProperties.configuration
                ?: org.apache.ibatis.session.Configuration().apply {
                    this.isCacheEnabled = true
                    this.isUseGeneratedKeys = true
                    this.isLazyLoadingEnabled = false
                    this.isMapUnderscoreToCamelCase = false
                    this.typeHandlerRegistry.setDefaultEnumTypeHandler(CustomEnumTypeHandler::class.java)
                    classes.forEach { this.typeAliasRegistry.registerAlias(it) }
                }
        factoryBean.setConfiguration(configuration)

        return factoryBean.getObject()
    }

    /**
     * we use TypeToken or TypeReference to keep the generic type info.
     * mybatis spring boot plugin may try to register them as normal typeAliases
     * it leads to exception like "org.apache.ibatis.type.TypeException: The alias '$special$$inlined$parse$1' is already mapped to the value 'InquiryAnswer$$special$$inlined$parse$1'"
     * Hence, instead of using wildcard typeAliasesPackage, we have to use our own scanClasses mechanism
     * see also:
     * org.mybatis.spring.SqlSessionFactoryBean#buildSqlSessionFactory
     */
    private fun filterTypeAliasesClasses(classes: Set<Class<*>>): List<Class<*>> {
        return classes
                // copy from org.mybatis.spring.SqlSessionFactoryBean#buildSqlSessionFactory
                .asSequence()
                .filter { clazz: Class<*> ->
                    // somehow this like may throw an error
                    // Caused by: java.lang.InternalError: Malformed class name
//                    !clazz.isAnonymousClass
                    try {
                        !clazz.isAnonymousClass
                    } catch (e: InternalError) {
                        false
                    }
                }
                .filter { clazz: Class<*> -> !clazz.isInterface }
                .filter { clazz: Class<*> -> !clazz.isMemberClass }
                // we add the following lines
//                .filter {
//                    @Suppress("UnstableApiUsage")
//                    !it.isAssignableFrom(TypeToken::class.java)
//                }
//                .filter {
//                    !it.isAssignableFrom(TypeReference::class.java)
//                }
                .filter { !it.simpleName.contains("\$\$inlined\$") }
                .toList()
    }

    /**
     * scan typeAlias classes
     * see also:
     * org.mybatis.spring.SqlSessionFactoryBean#scanClasses
     * @param packagePatterns
     * @param assignableType typeAliasesSuperType
     */
    private fun scanClasses(
            packagePatterns: String,
            assignableType: Class<*>? = null
    ): Set<Class<*>> {
        val RESOURCE_PATTERN_RESOLVER: ResourcePatternResolver = PathMatchingResourcePatternResolver()
        val METADATA_READER_FACTORY: MetadataReaderFactory = CachingMetadataReaderFactory()

        val classes: MutableSet<Class<*>> = HashSet()
        val packagePatternArray = StringUtils.tokenizeToStringArray(packagePatterns,
                ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS)
        for (packagePattern in packagePatternArray) {
            val resources = RESOURCE_PATTERN_RESOLVER.getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                    + ClassUtils.convertClassNameToResourcePath(packagePattern) + "/**/*.class")
            for (resource in resources) {
                try {
                    val classMetadata = METADATA_READER_FACTORY.getMetadataReader(resource).classMetadata
                    val clazz = Resources.classForName(classMetadata.className)
                    if (assignableType == null || assignableType.isAssignableFrom(clazz)) {
                        classes.add(clazz)
                    }
                } catch (e: Throwable) {
                    logger.warn("Cannot load the '$resource'. Cause by $e")
                }
            }
        }
        return classes
    }

}
