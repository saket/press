@file:kotlinx.cinterop.InteropStubs
@file:Suppress("UNUSED_VARIABLE", "UNUSED_EXPRESSION")
package cmark

import kotlin.native.SymbolName
import kotlinx.cinterop.internal.*
import kotlinx.cinterop.*
import cnames.structs.cmark_iter
import cnames.structs.cmark_node
import cnames.structs.cmark_parser
import platform.posix.FILE
import platform.posix.size_t

// NOTE THIS FILE IS AUTO-GENERATED

@CStruct("struct { void* p0; void* p1; void* p2; }")
class cmark_mem(rawPtr: NativePtr) : CStructVar(rawPtr) {
    
    companion object : CStructVar.Type(24, 8)
    
    var calloc: CPointer<CFunction<(size_t, size_t) -> COpaquePointer?>>?
        get() = memberAt<CPointerVar<CFunction<(size_t, size_t) -> COpaquePointer?>>>(0).value
        set(value) { memberAt<CPointerVar<CFunction<(size_t, size_t) -> COpaquePointer?>>>(0).value = value }
    
    var realloc: CPointer<CFunction<(COpaquePointer?, size_t) -> COpaquePointer?>>?
        get() = memberAt<CPointerVar<CFunction<(COpaquePointer?, size_t) -> COpaquePointer?>>>(8).value
        set(value) { memberAt<CPointerVar<CFunction<(COpaquePointer?, size_t) -> COpaquePointer?>>>(8).value = value }
    
    var free: CPointer<CFunction<(COpaquePointer?) -> Unit>>?
        get() = memberAt<CPointerVar<CFunction<(COpaquePointer?) -> Unit>>>(16).value
        set(value) { memberAt<CPointerVar<CFunction<(COpaquePointer?) -> Unit>>>(16).value = value }
}

enum class cmark_list_type(value: UInt) : CEnum {
    CMARK_NO_LIST(0u),
    CMARK_BULLET_LIST(1u),
    CMARK_ORDERED_LIST(2u),
    ;
    
    override val value: UInt = value
    
    companion object {
        
        fun byValue(value: UInt) = cmark_list_type.values().find { it.value == value }!!
    }
    
    class Var(rawPtr: NativePtr) : CEnumVar(rawPtr) {
        companion object : Type(UIntVar.size.toInt())
        var value: cmark_list_type
            get() = byValue(this.reinterpret<UIntVar>().value)
            set(value) { this.reinterpret<UIntVar>().value = value.value }
    }
}

enum class cmark_delim_type(value: UInt) : CEnum {
    CMARK_NO_DELIM(0u),
    CMARK_PERIOD_DELIM(1u),
    CMARK_PAREN_DELIM(2u),
    ;
    
    override val value: UInt = value
    
    companion object {
        
        fun byValue(value: UInt) = cmark_delim_type.values().find { it.value == value }!!
    }
    
    class Var(rawPtr: NativePtr) : CEnumVar(rawPtr) {
        companion object : Type(UIntVar.size.toInt())
        var value: cmark_delim_type
            get() = byValue(this.reinterpret<UIntVar>().value)
            set(value) { this.reinterpret<UIntVar>().value = value.value }
    }
}

enum class cmark_event_type(value: UInt) : CEnum {
    CMARK_EVENT_NONE(0u),
    CMARK_EVENT_DONE(1u),
    CMARK_EVENT_ENTER(2u),
    CMARK_EVENT_EXIT(3u),
    ;
    
    override val value: UInt = value
    
    companion object {
        
        fun byValue(value: UInt) = cmark_event_type.values().find { it.value == value }!!
    }
    
    class Var(rawPtr: NativePtr) : CEnumVar(rawPtr) {
        companion object : Type(UIntVar.size.toInt())
        var value: cmark_event_type
            get() = byValue(this.reinterpret<UIntVar>().value)
            set(value) { this.reinterpret<UIntVar>().value = value.value }
    }
}

@CCall("knifunptr_cmark0_cmark_markdown_to_html")
external fun cmark_markdown_to_html(@CCall.CString text: String?, len: size_t, options: Int): CPointer<ByteVar>?

@CCall("knifunptr_cmark1_cmark_get_default_mem_allocator")
external fun cmark_get_default_mem_allocator(): CPointer<cmark_mem>?

@CCall("knifunptr_cmark2_cmark_node_new")
external fun cmark_node_new(type: cmark_node_type): CPointer<cmark_node>?

@CCall("knifunptr_cmark3_cmark_node_new_with_mem")
external fun cmark_node_new_with_mem(type: cmark_node_type, mem: CValuesRef<cmark_mem>?): CPointer<cmark_node>?

@CCall("knifunptr_cmark4_cmark_node_free")
external fun cmark_node_free(node: CValuesRef<cmark_node>?): Unit

@CCall("knifunptr_cmark5_cmark_node_next")
external fun cmark_node_next(node: CValuesRef<cmark_node>?): CPointer<cmark_node>?

@CCall("knifunptr_cmark6_cmark_node_previous")
external fun cmark_node_previous(node: CValuesRef<cmark_node>?): CPointer<cmark_node>?

@CCall("knifunptr_cmark7_cmark_node_parent")
external fun cmark_node_parent(node: CValuesRef<cmark_node>?): CPointer<cmark_node>?

@CCall("knifunptr_cmark8_cmark_node_first_child")
external fun cmark_node_first_child(node: CValuesRef<cmark_node>?): CPointer<cmark_node>?

@CCall("knifunptr_cmark9_cmark_node_last_child")
external fun cmark_node_last_child(node: CValuesRef<cmark_node>?): CPointer<cmark_node>?

@CCall("knifunptr_cmark10_cmark_iter_new")
external fun cmark_iter_new(root: CValuesRef<cmark_node>?): CPointer<cmark_iter>?

@CCall("knifunptr_cmark11_cmark_iter_free")
external fun cmark_iter_free(iter: CValuesRef<cmark_iter>?): Unit

@CCall("knifunptr_cmark12_cmark_iter_next")
external fun cmark_iter_next(iter: CValuesRef<cmark_iter>?): cmark_event_type

@CCall("knifunptr_cmark13_cmark_iter_get_node")
external fun cmark_iter_get_node(iter: CValuesRef<cmark_iter>?): CPointer<cmark_node>?

@CCall("knifunptr_cmark14_cmark_iter_get_event_type")
external fun cmark_iter_get_event_type(iter: CValuesRef<cmark_iter>?): cmark_event_type

@CCall("knifunptr_cmark15_cmark_iter_get_root")
external fun cmark_iter_get_root(iter: CValuesRef<cmark_iter>?): CPointer<cmark_node>?

@CCall("knifunptr_cmark16_cmark_iter_reset")
external fun cmark_iter_reset(iter: CValuesRef<cmark_iter>?, current: CValuesRef<cmark_node>?, event_type: cmark_event_type): Unit

@CCall("knifunptr_cmark17_cmark_node_get_user_data")
external fun cmark_node_get_user_data(node: CValuesRef<cmark_node>?): COpaquePointer?

@CCall("knifunptr_cmark18_cmark_node_set_user_data")
external fun cmark_node_set_user_data(node: CValuesRef<cmark_node>?, user_data: CValuesRef<*>?): Int

@CCall("knifunptr_cmark19_cmark_node_get_type")
external fun cmark_node_get_type(node: CValuesRef<cmark_node>?): cmark_node_type

@CCall("knifunptr_cmark20_cmark_node_get_type_string")
external fun cmark_node_get_type_string(node: CValuesRef<cmark_node>?): CPointer<ByteVar>?

@CCall("knifunptr_cmark21_cmark_node_get_literal")
external fun cmark_node_get_literal(node: CValuesRef<cmark_node>?): CPointer<ByteVar>?

@CCall("knifunptr_cmark22_cmark_node_set_literal")
external fun cmark_node_set_literal(node: CValuesRef<cmark_node>?, @CCall.CString content: String?): Int

@CCall("knifunptr_cmark23_cmark_node_get_heading_level")
external fun cmark_node_get_heading_level(node: CValuesRef<cmark_node>?): Int

@CCall("knifunptr_cmark24_cmark_node_set_heading_level")
external fun cmark_node_set_heading_level(node: CValuesRef<cmark_node>?, level: Int): Int

@CCall("knifunptr_cmark25_cmark_node_get_list_type")
external fun cmark_node_get_list_type(node: CValuesRef<cmark_node>?): cmark_list_type

@CCall("knifunptr_cmark26_cmark_node_set_list_type")
external fun cmark_node_set_list_type(node: CValuesRef<cmark_node>?, type: cmark_list_type): Int

@CCall("knifunptr_cmark27_cmark_node_get_list_delim")
external fun cmark_node_get_list_delim(node: CValuesRef<cmark_node>?): cmark_delim_type

@CCall("knifunptr_cmark28_cmark_node_set_list_delim")
external fun cmark_node_set_list_delim(node: CValuesRef<cmark_node>?, delim: cmark_delim_type): Int

@CCall("knifunptr_cmark29_cmark_node_get_list_start")
external fun cmark_node_get_list_start(node: CValuesRef<cmark_node>?): Int

@CCall("knifunptr_cmark30_cmark_node_set_list_start")
external fun cmark_node_set_list_start(node: CValuesRef<cmark_node>?, start: Int): Int

@CCall("knifunptr_cmark31_cmark_node_get_list_tight")
external fun cmark_node_get_list_tight(node: CValuesRef<cmark_node>?): Int

@CCall("knifunptr_cmark32_cmark_node_set_list_tight")
external fun cmark_node_set_list_tight(node: CValuesRef<cmark_node>?, tight: Int): Int

@CCall("knifunptr_cmark33_cmark_node_get_fence_info")
external fun cmark_node_get_fence_info(node: CValuesRef<cmark_node>?): CPointer<ByteVar>?

@CCall("knifunptr_cmark34_cmark_node_set_fence_info")
external fun cmark_node_set_fence_info(node: CValuesRef<cmark_node>?, @CCall.CString info: String?): Int

@CCall("knifunptr_cmark35_cmark_node_get_url")
external fun cmark_node_get_url(node: CValuesRef<cmark_node>?): CPointer<ByteVar>?

@CCall("knifunptr_cmark36_cmark_node_set_url")
external fun cmark_node_set_url(node: CValuesRef<cmark_node>?, @CCall.CString url: String?): Int

@CCall("knifunptr_cmark37_cmark_node_get_title")
external fun cmark_node_get_title(node: CValuesRef<cmark_node>?): CPointer<ByteVar>?

@CCall("knifunptr_cmark38_cmark_node_set_title")
external fun cmark_node_set_title(node: CValuesRef<cmark_node>?, @CCall.CString title: String?): Int

@CCall("knifunptr_cmark39_cmark_node_get_on_enter")
external fun cmark_node_get_on_enter(node: CValuesRef<cmark_node>?): CPointer<ByteVar>?

@CCall("knifunptr_cmark40_cmark_node_set_on_enter")
external fun cmark_node_set_on_enter(node: CValuesRef<cmark_node>?, @CCall.CString on_enter: String?): Int

@CCall("knifunptr_cmark41_cmark_node_get_on_exit")
external fun cmark_node_get_on_exit(node: CValuesRef<cmark_node>?): CPointer<ByteVar>?

@CCall("knifunptr_cmark42_cmark_node_set_on_exit")
external fun cmark_node_set_on_exit(node: CValuesRef<cmark_node>?, @CCall.CString on_exit: String?): Int

@CCall("knifunptr_cmark43_cmark_node_get_start_line")
external fun cmark_node_get_start_line(node: CValuesRef<cmark_node>?): Int

@CCall("knifunptr_cmark44_cmark_node_get_start_column")
external fun cmark_node_get_start_column(node: CValuesRef<cmark_node>?): Int

@CCall("knifunptr_cmark45_cmark_node_get_end_line")
external fun cmark_node_get_end_line(node: CValuesRef<cmark_node>?): Int

@CCall("knifunptr_cmark46_cmark_node_get_end_column")
external fun cmark_node_get_end_column(node: CValuesRef<cmark_node>?): Int

@CCall("knifunptr_cmark47_cmark_node_unlink")
external fun cmark_node_unlink(node: CValuesRef<cmark_node>?): Unit

@CCall("knifunptr_cmark48_cmark_node_insert_before")
external fun cmark_node_insert_before(node: CValuesRef<cmark_node>?, sibling: CValuesRef<cmark_node>?): Int

@CCall("knifunptr_cmark49_cmark_node_insert_after")
external fun cmark_node_insert_after(node: CValuesRef<cmark_node>?, sibling: CValuesRef<cmark_node>?): Int

@CCall("knifunptr_cmark50_cmark_node_replace")
external fun cmark_node_replace(oldnode: CValuesRef<cmark_node>?, newnode: CValuesRef<cmark_node>?): Int

@CCall("knifunptr_cmark51_cmark_node_prepend_child")
external fun cmark_node_prepend_child(node: CValuesRef<cmark_node>?, child: CValuesRef<cmark_node>?): Int

@CCall("knifunptr_cmark52_cmark_node_append_child")
external fun cmark_node_append_child(node: CValuesRef<cmark_node>?, child: CValuesRef<cmark_node>?): Int

@CCall("knifunptr_cmark53_cmark_consolidate_text_nodes")
external fun cmark_consolidate_text_nodes(root: CValuesRef<cmark_node>?): Unit

@CCall("knifunptr_cmark54_cmark_parser_new")
external fun cmark_parser_new(options: Int): CPointer<cmark_parser>?

@CCall("knifunptr_cmark55_cmark_parser_new_with_mem")
external fun cmark_parser_new_with_mem(options: Int, mem: CValuesRef<cmark_mem>?): CPointer<cmark_parser>?

@CCall("knifunptr_cmark56_cmark_parser_free")
external fun cmark_parser_free(parser: CValuesRef<cmark_parser>?): Unit

@CCall("knifunptr_cmark57_cmark_parser_feed")
external fun cmark_parser_feed(parser: CValuesRef<cmark_parser>?, @CCall.CString buffer: String?, len: size_t): Unit

@CCall("knifunptr_cmark58_cmark_parser_finish")
external fun cmark_parser_finish(parser: CValuesRef<cmark_parser>?): CPointer<cmark_node>?

@CCall("knifunptr_cmark59_cmark_parse_document")
external fun cmark_parse_document(@CCall.CString buffer: String?, len: size_t, options: Int): CPointer<cmark_node>?

@CCall("knifunptr_cmark60_cmark_parse_file")
external fun cmark_parse_file(f: CValuesRef<FILE>?, options: Int): CPointer<cmark_node>?

@CCall("knifunptr_cmark61_cmark_render_xml")
external fun cmark_render_xml(root: CValuesRef<cmark_node>?, options: Int): CPointer<ByteVar>?

@CCall("knifunptr_cmark62_cmark_render_html")
external fun cmark_render_html(root: CValuesRef<cmark_node>?, options: Int): CPointer<ByteVar>?

@CCall("knifunptr_cmark63_cmark_render_man")
external fun cmark_render_man(root: CValuesRef<cmark_node>?, options: Int, width: Int): CPointer<ByteVar>?

@CCall("knifunptr_cmark64_cmark_render_commonmark")
external fun cmark_render_commonmark(root: CValuesRef<cmark_node>?, options: Int, width: Int): CPointer<ByteVar>?

@CCall("knifunptr_cmark65_cmark_render_latex")
external fun cmark_render_latex(root: CValuesRef<cmark_node>?, options: Int, width: Int): CPointer<ByteVar>?

@CCall("knifunptr_cmark66_cmark_version")
external fun cmark_version(): Int

@CCall("knifunptr_cmark67_cmark_version_string")
external fun cmark_version_string(): CPointer<ByteVar>?

const val CMARK_VERSION: Int = 7424

val CMARK_VERSION_STRING: String get() = "0.29.0"

const val CMARK_NODE_HEADER: Int = 9

const val CMARK_NODE_HRULE: Int = 10

const val CMARK_NODE_HTML: Int = 6

const val CMARK_NODE_INLINE_HTML: Int = 15

const val CMARK_OPT_DEFAULT: Int = 0

const val CMARK_OPT_SOURCEPOS: Int = 2

const val CMARK_OPT_HARDBREAKS: Int = 4

const val CMARK_OPT_SAFE: Int = 8

const val CMARK_OPT_UNSAFE: Int = 131072

const val CMARK_OPT_NOBREAKS: Int = 16

const val CMARK_OPT_NORMALIZE: Int = 256

const val CMARK_OPT_VALIDATE_UTF8: Int = 512

const val CMARK_OPT_SMART: Int = 1024

const val NODE_DOCUMENT: Int = 1

const val NODE_BLOCK_QUOTE: Int = 2

const val NODE_LIST: Int = 3

const val NODE_ITEM: Int = 4

const val NODE_CODE_BLOCK: Int = 5

const val NODE_HTML_BLOCK: Int = 6

const val NODE_CUSTOM_BLOCK: Int = 7

const val NODE_PARAGRAPH: Int = 8

const val NODE_HEADING: Int = 9

const val NODE_HEADER: Int = 9

const val NODE_THEMATIC_BREAK: Int = 10

const val NODE_HRULE: Int = 10

const val NODE_TEXT: Int = 11

const val NODE_SOFTBREAK: Int = 12

const val NODE_LINEBREAK: Int = 13

const val NODE_CODE: Int = 14

const val NODE_HTML_INLINE: Int = 15

const val NODE_CUSTOM_INLINE: Int = 16

const val NODE_EMPH: Int = 17

const val NODE_STRONG: Int = 18

const val NODE_LINK: Int = 19

const val NODE_IMAGE: Int = 20

const val BULLET_LIST: Int = 1

const val ORDERED_LIST: Int = 2

const val PERIOD_DELIM: Int = 1

const val PAREN_DELIM: Int = 2

val cmark_node_get_header_level: CPointer<CFunction<(CPointer<cmark_node>?) -> Int>>?
    @CCall("knifunptr_cmark68_cmark_node_get_header_level_getter") external get

val cmark_node_set_header_level: CPointer<CFunction<(CPointer<cmark_node>?, Int) -> Int>>?
    @CCall("knifunptr_cmark69_cmark_node_set_header_level_getter") external get


val CMARK_NODE_NONE: cmark_node_type get() = 0u

val CMARK_NODE_DOCUMENT: cmark_node_type get() = 1u

val CMARK_NODE_BLOCK_QUOTE: cmark_node_type get() = 2u

val CMARK_NODE_LIST: cmark_node_type get() = 3u

val CMARK_NODE_ITEM: cmark_node_type get() = 4u

val CMARK_NODE_CODE_BLOCK: cmark_node_type get() = 5u

val CMARK_NODE_HTML_BLOCK: cmark_node_type get() = 6u

val CMARK_NODE_CUSTOM_BLOCK: cmark_node_type get() = 7u

val CMARK_NODE_PARAGRAPH: cmark_node_type get() = 8u

val CMARK_NODE_HEADING: cmark_node_type get() = 9u

val CMARK_NODE_THEMATIC_BREAK: cmark_node_type get() = 10u

val CMARK_NODE_FIRST_BLOCK: cmark_node_type get() = 1u

val CMARK_NODE_LAST_BLOCK: cmark_node_type get() = 10u

val CMARK_NODE_TEXT: cmark_node_type get() = 11u

val CMARK_NODE_SOFTBREAK: cmark_node_type get() = 12u

val CMARK_NODE_LINEBREAK: cmark_node_type get() = 13u

val CMARK_NODE_CODE: cmark_node_type get() = 14u

val CMARK_NODE_HTML_INLINE: cmark_node_type get() = 15u

val CMARK_NODE_CUSTOM_INLINE: cmark_node_type get() = 16u

val CMARK_NODE_EMPH: cmark_node_type get() = 17u

val CMARK_NODE_STRONG: cmark_node_type get() = 18u

val CMARK_NODE_LINK: cmark_node_type get() = 19u

val CMARK_NODE_IMAGE: cmark_node_type get() = 20u

val CMARK_NODE_FIRST_INLINE: cmark_node_type get() = 11u

val CMARK_NODE_LAST_INLINE: cmark_node_type get() = 20u

typealias cmark_node_typeVar = UIntVarOf<cmark_node_type>

typealias cmark_node_type = UInt
