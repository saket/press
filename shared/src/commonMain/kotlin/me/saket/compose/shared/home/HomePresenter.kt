package me.saket.compose.shared.home

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.observableOfEmpty

class HomePresenter {

  fun Observable<HomeEvent>.toContentModels(): Observable<HomeContentModel> {
    return observableOfEmpty()
  }
}