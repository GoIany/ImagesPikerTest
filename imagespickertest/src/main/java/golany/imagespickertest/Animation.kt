package golany.imagespickertest

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.doOnEnd

object Animation {

    fun slide(view: View, isVertical: Boolean, currentDimen: Int, newDimen: Int, doOnEnd: () -> Unit = {}){
        val valueAnimator = ValueAnimator.ofInt(currentDimen, newDimen).apply {
            addUpdateListener {
                if(isVertical) view.layoutParams.height = it.animatedValue as Int
                else view.layoutParams.width = it.animatedValue as Int
                view.requestLayout()
            }
        }
        valueAnimator.doOnEnd {
            doOnEnd()
        }

        AnimatorSet().apply {
            interpolator = AccelerateDecelerateInterpolator()
            play(valueAnimator)
        }.start()
    }

    fun slideShow(view: View, isVertical: Boolean, showDimen: Int, doOnEnd: () -> Unit = {}){ slide(view, isVertical, 0, showDimen, doOnEnd) }

    fun slideHide(view: View, isVertical: Boolean, showDimen: Int){ slide(view, isVertical, showDimen, 0) }

}