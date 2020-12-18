package org.hyperskill.pomodoro

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.core.app.NotificationCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import org.hyperskill.pomodoro.timer.TimerView


class MainActivity : FragmentActivity(),
        SettingsDialogFragment.SettingsDialogListener,
        TimerView.TimerListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val timerView = findViewById<TimerView>(R.id.timerView)

        this.startCounter(timerView)
        this.resetCounter(timerView)
        this.counterSettings(timerView)
    }

    private fun startCounter(timerView: TimerView) {
        findViewById<Button>(R.id.startButton).apply {
            setOnClickListener {
                timerView.stop()
                timerView.start()
            }
        }
    }

    private fun resetCounter(timerView: TimerView) {
        findViewById<Button>(R.id.resetButton).apply {
            setOnClickListener {
                timerView.stop()
            }
        }
    }

    private fun counterSettings(timerView: TimerView) {
        findViewById<Button>(R.id.settingsButton).apply {
            setOnClickListener {
                timerView.stop()
                showSettingsDialog()
            }
        }
    }

    private fun showSettingsDialog() {
        val dialog = SettingsDialogFragment()
        dialog.show(supportFragmentManager, "SettingsDialogFragment")
    }

    override fun onDialogPositiveClick(dialog: DialogFragment, workTime: Int?, restTime: Int?) {
        val timerView = findViewById<TimerView>(R.id.timerView)
        timerView.configure(workTime, restTime)
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        dialog.dismiss()
    }

    override fun onJobStart() {
        findViewById<Button>(R.id.settingsButton).isEnabled = false
    }

    override fun onCycleChange(cycle: Int) {

        var title = "Time to work!"
        var content = "Work hard"

        if (cycle % 2 == 0) {
            title = "Time to rest!"
            content = "Rest in peace"
        }

        val NOTIFICATION_ID = 234
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val CHANNEL_ID = "cycle"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "cycle"
            val Description = "This is my channel"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            mChannel.description = Description
            mChannel.enableLights(true)
            mChannel.lightColor = Color.RED
            mChannel.enableVibration(true)
            mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            mChannel.setShowBadge(false)
            manager.createNotificationChannel(mChannel)
        }

        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.alert_dark_frame)
                .setContentTitle(title)
                .setContentText(content)

        val notificationIntent = Intent(this, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(contentIntent)

        manager.notify(NOTIFICATION_ID, builder.build())
    }

    override fun onJobDone() {
        findViewById<Button>(R.id.settingsButton).isEnabled = true
    }

    override fun onRestPeriodStart() {
        TODO("Not yet implemented")
    }

    override fun onWorkPeriodStart() {
        TODO("Not yet implemented")
    }
}