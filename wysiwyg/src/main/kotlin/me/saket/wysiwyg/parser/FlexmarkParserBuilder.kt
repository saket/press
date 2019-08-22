package me.saket.wysiwyg.parser

import com.vladsch.flexmark.parser.LinkRefProcessorFactory
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.parser.delimiter.DelimiterProcessor
import com.vladsch.flexmark.util.builder.Extension
import com.vladsch.flexmark.util.data.DataKey
import com.vladsch.flexmark.util.data.MutableDataSet

/**
 * Wrapper around [Parser] because its builder doesn't allow updating [Parser.options].
 */
class FlexmarkParserBuilder {

  private val extensions = ArrayList<Extension>()
  private val options = MutableDataSet()
  private var delimiterProcessor: DelimiterProcessor? = null
  private var linkRefProcessorFactory: LinkRefProcessorFactory? = null

  fun addExtension(extension: Extension): FlexmarkParserBuilder {
    extensions += extension
    return this
  }

  fun <T> addOption(
    key: DataKey<T>,
    value: T
  ): FlexmarkParserBuilder {
    options.set(key, value)
    return this
  }

  fun setDelimiterProcessor(processor: DelimiterProcessor?): FlexmarkParserBuilder {
    this.delimiterProcessor = processor
    return this
  }

  fun build(): Parser {
    return Parser.builder(options)
        .extensions(extensions)
        .apply {
          if (delimiterProcessor != null) {
            customDelimiterProcessor(delimiterProcessor)
          }
          if (linkRefProcessorFactory != null) {
            linkRefProcessorFactory(linkRefProcessorFactory)
          }
        }
        .build()
  }
}