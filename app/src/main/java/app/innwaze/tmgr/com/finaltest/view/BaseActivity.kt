package app.innwaze.tmgr.com.finaltest.view

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

abstract class BaseActivity<B : ViewDataBinding> : AppCompatActivity() {
    protected lateinit var binding: B

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, provideLayoutRes())
    }

    abstract fun provideLayoutRes(): Int

}