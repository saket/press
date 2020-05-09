//
//  AppDelegate.swift
//  mac
//
//  Created by Saket Narayan on 4/15/20.
//  Copyright © 2020 Saket Narayan. All rights reserved.
//

import Cocoa
import SwiftUI
import shared
import Swinject
import Combine

@NSApplicationMain
class PressApp: NSObject, NSApplicationDelegate {

  var window: NSWindow!
  static var component: Resolver!

  func applicationDidFinishLaunching(_ aNotification: Notification) {
    PressApp.component = createAppComponent()

    let theme = PressApp.component.resolve(AppTheme.self)!
    let homeView = HomeView().environmentObject(theme)

    window = NSWindow(
      contentRect: NSRect(origin: CGPoint(x: 0, y: 0), size: Dimensions.window),
      styleMask: [.titled, .closable, .miniaturizable, .resizable, .fullSizeContentView],
      backing: .buffered, defer: false)
    window.center()
    window.setFrameAutosaveName("Main Window")
    window.contentView = NSHostingView(rootView: homeView)
    window.makeKeyAndOrderFront(nil)

    window.backgroundColor = NSColor(theme.palette.window.backgroundColor)
    window.titlebarAppearsTransparent = true
    window.isMovableByWindowBackground = true   // Dragging is difficult without the toolbar.
  }

  func applicationWillTerminate(_ aNotification: Notification) {
    PressApp.component = nil
  }

  // Sets up dependency injection for the app. I'm using the
  // term "component" to keep it consistent with the shared
  // Kotlin and Android code.
  func createAppComponent() -> Resolver {
    SharedAppComponent().initialize()
    return Assembler([
      ThemeComponent(),
      HomeComponent(),
      EditorComponent()
    ]).resolver
  }
}
