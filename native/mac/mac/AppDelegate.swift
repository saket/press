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

@NSApplicationMain
class AppDelegate: NSObject, NSApplicationDelegate {

  var window: NSWindow!
  lazy var component = createAppComponent()

  func applicationDidFinishLaunching(_ aNotification: Notification) {
    // Create the SwiftUI view that provides the window contents.
    let contentView = component.resolve(HomeView.self)!

    // Create the window and set the content view.
    window = NSWindow(
      contentRect: NSRect(x: 0, y: 0, width: 480, height: 300),
      styleMask: [.titled, .closable, .miniaturizable, .resizable, .fullSizeContentView],
      backing: .buffered, defer: false)
    window.center()
    window.setFrameAutosaveName("Main Window")
    window.contentView = NSHostingView(rootView: contentView)
    window.makeKeyAndOrderFront(nil)
  }

  func applicationWillTerminate(_ aNotification: Notification) {
    // Insert code here to tear down your application
  }

  // Sets up dependency injection for the app. I'm using the
  // term "component" to keep them consistent with the shared
  // Kotlin and Android code.
  func createAppComponent() -> Resolver {
    SharedAppComponent().initialize()
    return Assembler([
      HomeComponent()
    ]).resolver
  }
}
