package com.example.affirmations

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * 主界面（实验六：多Fragment应用）
 *
 * 使用 BottomNavigationView 在三个 Fragment 之间切换：
 *  - 个人信息（PersonInfoFragment）
 *  - 两张牌翻牌游戏（TwoCardGameFragment）
 *  - 三张牌翻牌游戏（ThreeCardGameFragment）
 * 个人资料与游戏历史均以对象序列化方式存储，退出后再次打开可恢复。
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            switchTo(PersonInfoFragment())
        }

        findViewById<BottomNavigationView>(R.id.navView)
            .setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_person -> switchTo(PersonInfoFragment())
                    R.id.nav_two_card -> switchTo(TwoCardGameFragment())
                    R.id.nav_three_card -> switchTo(ThreeCardGameFragment())
                    else -> return@setOnItemSelectedListener false
                }
                true
            }
    }

    private fun switchTo(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_about -> {
                showAboutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAboutDialog() {
        val message = getString(
            R.string.about_message,
            getString(R.string.app_name),
            getString(R.string.default_name),
            getString(R.string.default_student_id)
        )
        AlertDialog.Builder(this)
            .setTitle(R.string.about_title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
}
