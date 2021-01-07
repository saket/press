package press

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.RxWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.reactivex.Single
import me.saket.press.shared.syncWithResultRx2
import java.time.Duration

class BackgroundSyncWorker(
  appContext: Context,
  workerParams: WorkerParameters
) : RxWorker(appContext, workerParams) {

  override fun createWork(): Single<Result> {
    return PressApp.component.syncCoordinator()
      .syncWithResultRx2()
      .toSingleDefault(Result.success())
      .onErrorReturnItem(Result.failure())
  }

  companion object {
    fun schedule(workManager: WorkManager) {
      val request = PeriodicWorkRequest.Builder(
        BackgroundSyncWorker::class.java,
        Duration.ofMinutes(15)
      ).build()

      workManager.enqueueUniquePeriodicWork(
        "BackgroundSyncWorker",
        // Replace is useful in case the old worker's class was
        // moved. WorkManager will fail indefinitely otherwise.
        ExistingPeriodicWorkPolicy.REPLACE,
        request
      )
    }
  }
}
