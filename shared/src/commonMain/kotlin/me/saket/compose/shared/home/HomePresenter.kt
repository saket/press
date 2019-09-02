package me.saket.compose.shared.home

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.observableOf
import me.saket.compose.shared.Presenter

class HomePresenter : Presenter<HomeEvent, HomeUiModel> {

  override fun contentModels(events: Observable<HomeEvent>): Observable<HomeUiModel> {
    return observableOf(HomeUiModel("Woo!"))
  }
}