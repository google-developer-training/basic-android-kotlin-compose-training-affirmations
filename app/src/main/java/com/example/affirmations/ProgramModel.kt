package com.example.affirmations

import android.content.Context

/**
 * Model 层：专业咨询数据仓库（Program Adviser 的数据模型）
 *
 * 职责：
 *  - 维护“专业关键字 -> 咨询建议文案”的数据；
 *  - 提供按关键字查询的业务方法 query(...)。
 *
 * 约束：
 *  - Model 不持有任何 View 引用，不直接操作界面；
 *  - 文案不硬编码在 Kotlin 代码中，只持有字符串资源 ID，
 *    由 Controller 在查询时通过 Context 取出，符合实验“字符串资源间接引用”的要求。
 */
class ProgramModel {

    /** 一条专业记录：关键字（用于匹配） + 建议文案的字符串资源 ID */
    data class Program(
        val keyword: String,
        val adviceResId: Int
    )

    /** 内置专业咨询数据（模拟模型层返回的业务数据） */
    private val programs = listOf(
        Program("计算机", R.string.advice_cs),
        Program("计算机科学", R.string.advice_cs),
        Program("computer", R.string.advice_cs),
        Program("软件工程", R.string.advice_se),
        Program("软件", R.string.advice_se),
        Program("software", R.string.advice_se),
        Program("人工智能", R.string.advice_ai),
        Program("ai", R.string.advice_ai),
        Program("数据科学", R.string.advice_ds),
        Program("大数据", R.string.advice_ds),
        Program("data", R.string.advice_ds),
        Program("网络工程", R.string.advice_net),
        Program("网络", R.string.advice_net),
        Program("network", R.string.advice_net)
    )

    /**
     * 根据用户输入的关键字查询专业建议。
     * @param context 用于取字符串资源（由 Controller 传入）
     * @param keyword 用户输入的专业名称
     * @return 命中则返回对应建议文案；未命中返回未找到提示；空输入返回默认提示
     */
    fun query(context: Context, keyword: String): String {
        val kw = keyword.trim()
        if (kw.isEmpty()) {
            return context.getString(R.string.result_hint)
        }
        // 双向包含匹配：输入包含关键字 或 关键字包含输入，均视为命中
        for (p in programs) {
            if (kw.contains(p.keyword, ignoreCase = true) ||
                p.keyword.contains(kw, ignoreCase = true)) {
                return context.getString(p.adviceResId)
            }
        }
        return context.getString(R.string.advice_not_found, kw)
    }
}
