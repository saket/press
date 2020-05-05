import Combine
import SwiftUI

/// Copied from:
/// https://gist.github.com/unnamedd/6e8c3fbc806b8deb60fa65d6b9affab0
struct MacEditorTextField: NSViewRepresentable {
  @Binding var text: String

  let onSetup: (NSTextView) -> Void
  var onEditingChanged: () -> Void = {}
  var onCommit: () -> Void = {}
  var onTextChange: (String) -> Void = { _ in }

  public init(text: Binding<String>, onSetup: @escaping (NSTextView) -> Void = { _ in }) {
    self._text = text
    self.onSetup = onSetup
  }

  func makeCoordinator() -> Coordinator {
    Coordinator(self)
  }

  func makeNSView(context: Context) -> CustomTextView {
    let textView = CustomTextView(text: self.text, onSetup: onSetup)
    textView.delegate = context.coordinator
    return textView
  }

  func updateNSView(_ view: CustomTextView, context: Context) {
    view.text = text
    view.selectedRanges = context.coordinator.selectedRanges
  }
}

private struct TextProperties {
  let isRichText: Bool
  let isEditable: Bool
  let font: NSFont
}

#if DEBUG
struct MacEditorTextView_Previews: PreviewProvider {
  static var previews: some View {
    Group {
      MacEditorTextField(text: .constant("{ \n    planets { \n        name \n    }\n}"))
        .environment(\.colorScheme, .dark)
        .previewDisplayName("Dark Mode")
    }
  }
}
#endif

extension MacEditorTextField {
  class Coordinator: NSObject, NSTextViewDelegate {
    var parent: MacEditorTextField
    var selectedRanges: [NSValue] = []

    init(_ parent: MacEditorTextField) {
      self.parent = parent
    }

    func textDidBeginEditing(_ notification: Notification) {
      guard let textView = notification.object as? NSTextView else {
        return
      }

      self.parent.text = textView.string
      self.parent.onEditingChanged()
    }

    func textDidChange(_ notification: Notification) {
      guard let textView = notification.object as? NSTextView else {
        return
      }

      self.parent.text = textView.string
      self.selectedRanges = textView.selectedRanges
    }

    func textDidEndEditing(_ notification: Notification) {
      guard let textView = notification.object as? NSTextView else {
        return
      }

      self.parent.text = textView.string
      self.parent.onCommit()
    }
  }
}

final class CustomTextView: NSView {
  private var onSetupTextView: (NSTextView) -> Void
  weak var delegate: NSTextViewDelegate?

  var text: String {
    didSet {
      textView.string = text
    }
  }

  var selectedRanges: [NSValue] = [] {
    didSet {
      guard selectedRanges.count > 0 else {
        return
      }
      textView.selectedRanges = selectedRanges
    }
  }

  private lazy var scrollView: NSScrollView = {
    let scrollView = NSScrollView()
    scrollView.drawsBackground = false
    scrollView.borderType = .noBorder
    scrollView.hasVerticalScroller = true
    scrollView.hasHorizontalRuler = false
    scrollView.autoresizingMask = [.width, .height]
    scrollView.translatesAutoresizingMaskIntoConstraints = false
    return scrollView
  }()

  private lazy var textView: NSTextView = {
    let contentSize = scrollView.contentSize
    let textStorage = NSTextStorage()

    let layoutManager = NSLayoutManager()
    textStorage.addLayoutManager(layoutManager)

    let textContainer = NSTextContainer(containerSize: scrollView.frame.size)
    textContainer.widthTracksTextView = true
    textContainer.containerSize = NSSize(
      width: contentSize.width,
      height: CGFloat.greatestFiniteMagnitude
    )

    layoutManager.addTextContainer(textContainer)

    let textView = NSTextView(frame: .zero, textContainer: textContainer)
    textView.autoresizingMask = .width
    textView.delegate = self.delegate
    textView.isHorizontallyResizable = false
    textView.isVerticallyResizable = true
    textView.maxSize = NSSize(width: CGFloat.greatestFiniteMagnitude, height: CGFloat.greatestFiniteMagnitude)
    textView.minSize = NSSize(width: 0, height: contentSize.height)
    textView.textColor = NSColor.labelColor
    textView.backgroundColor = .clear

    return textView
  }()

  // MARK: - Init
  init(
    text: String,
    isEditable: Bool = true,
    font: NSFont = NSFont.systemFont(ofSize: 32, weight: .ultraLight),
    isRichText: Bool = true,
    onSetup: @escaping (NSTextView) -> Void
  ) {
    self.text = text
    self.onSetupTextView = onSetup
    super.init(frame: .zero)
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  // MARK: - Life cycle

  override func viewWillDraw() {
    super.viewWillDraw()

    setupScrollViewConstraints()
    setupTextView()
  }

  func setupScrollViewConstraints() {
    scrollView.translatesAutoresizingMaskIntoConstraints = false

    addSubview(scrollView)

    NSLayoutConstraint.activate([
      scrollView.topAnchor.constraint(equalTo: topAnchor),
      scrollView.trailingAnchor.constraint(equalTo: trailingAnchor),
      scrollView.bottomAnchor.constraint(equalTo: bottomAnchor),
      scrollView.leadingAnchor.constraint(equalTo: leadingAnchor)
    ])
  }

  func setupTextView() {
    scrollView.documentView = textView
  }
}
