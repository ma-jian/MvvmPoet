

/**
 * 创建对象
 * kotlin 支持类型推断，可以将变量后的类型去掉 : val demo = Demo("new 对象")
 */
val demo: Demo? = Demo("new 对象")
//避免空指针的异常，kotlin在调用方进行处理——安全的调用。通过?.的方式进行调用。
demo?.hello()

//!!是非空断言运算符，也是避免空指针异常的手段方式之一，若该值为空则抛出异常。
demo!!.offer(1) {
    println("我优先于1，执行")
    //内联函数
    "".let {  }
}




//主构造的参数可以在初始化块中使用。它们也可以在类体内声明的属性初始化器中使用：
class Demo constructor(str: String) : Base(), MyInterface {
    //声明的属性可以是可变的（var）或只读的（val）。
    private val read: String = "" //对一个只读的参数赋值为空无意义 'read' is always non-null type
        get() = "$field-我是getter"

    private var write: String? = ""
        get() {
            return "$field-我可以改变该字段的返回值"
        }
        set(value) {
            field = "$value-我可以改变该字段的赋值"
        }

    /**
     * 类型后面添加？表示该字段可以null类型，该属性不可以赋值给一个非空类型的字段、否则会报警告-> 为了空安全
     * 此构造中String类型的 str 如果在类型后面添加 ? 则会报警告吗，因为主构造中该字段为非空。
     * constructor(str: String?, i: Int?) : this(str)
     */
    constructor(str: String, i: Int?) : this(str) {
        println("我是次构造函数 $str ; $i")
    }

    init {
        //主构造的参数可以在初始化块中使用
        println("我是初始化代码块....$str")
    }

    fun hello(): String? {
        println("hello,kotlin $write")
        return write
    }

    override val size: Int
        get() = 1

    override fun add() {

    }
}

interface MyInterface {
    val size: Int // 抽象的
    fun add()
    fun print() {
        println("size:$size")
    }
}

open class Base {
    fun pop() {
        println("Base : pop")
    }

    //先执行block在进行打印
    fun offer(i: Int, block: () -> Unit) {
        block.invoke()
        println("$i")
    }
}