# Navigation Fragments Formula Sample

This sample demonstrates infinite fragment navigation using the Formula framework for UI state management.

## Features

- **Infinite Routing**: Start with fragment 0 and navigate to infinitely many fragments (1, 2, 3, ...)
- **Counter State**: Each fragment manages its own counter state locally in its Formula
- **Global Event System**: A global store manages counter increment events via Kotlin Flows
- **Fragment Navigation**: Each fragment can navigate to the next fragment or back in the navigation stack
- **Counter Management**: Each fragment can increment any fragment counter through the global event system
- **Compose UI in Fragments**: Fragment content is rendered with Jetpack Compose via `ComposeViewFactory`
- **ViewModel Bridge**: Activity navigation is handled by the Activity in response to Formula-driven events exposed via a `ViewModel`

## Architecture

### Core Components

1. **CounterFragmentKey**: Parcelable key that identifies fragments using integer IDs
2. **NavigationActivityFormula**: Activity-level Formula that manages:
   - Navigation state (current fragment, back stack)
   - Global counter increment events via `SharedFlow`
   - Outputs navigation and counter APIs consumed by fragments
3. **NavigationViewModel**: Hosts `NavigationActivityFormula`, exposes its `Output` as a `StateFlow`, and emits navigation events for the
   Activity
4. **NavigationActivity**: Observes navigation events and performs fragment transactions
5. **NavigationActivityComponent**: Bridges Activity store context to fragments, exposes `FragmentState` and provides
   `CounterFragmentFormula.Dependencies`
6. **CounterFragmentFormula**: Manages per-fragment state including:
   - Local counter state
   - Subscriptions to navigation stack and counter increment events
7. **CounterFragmentFeatureFactory**: Binds fragment key to Formula feature
8. **CounterFragmentViewFactory**: Compose-based view factory for fragments (uses `CounterScreen`)
9. **CounterScreen**: Composable that renders the fragment UI

### State Management Architecture

**Activity-Level State:**

- Navigation stack managed in `NavigationActivityFormula.State` and kept in sync with `FragmentState`
- Counter increment events transmitted via `SharedFlow<Int>` within the activity formula
- Navigation actions are requested from fragments via formula outputs and forwarded to the Activity through a `ViewModel`

**Local State (per fragment):**

- Each fragment's counter is managed locally in its `CounterFragmentFormula.State`
- Counter starts at 0 and increments when global increment events are received for this fragment

### UI Elements

Each fragment uses Compose and contains:
- **Fragment Title**: Shows "Fragment X" where X is the fragment ID
- **Counter Display**: Shows the current counter value for this fragment (locally managed)
- **Navigate to Next Fragment Button**: Creates and navigates to the next fragment (ID + 1)
- **Navigate Back Button**: Goes back in the navigation stack (hidden for the root fragment)
- **Fragment Counter Buttons**: Buttons for all fragments in the navigation stack (including current) that send global increment events

### Navigation Flow

1. App starts with Fragment 0 (counter = 0)
2. User can tap "Increment Counter for Fragment 0" to increment the current fragment's counter
3. User can tap "Navigate to Next Fragment" to create Fragment 1, 2, 3, etc.
4. User can tap "Navigate Back" to go back through the navigation stack
5. User can tap "Increment Counter for Fragment X" to send global events to any fragment
6. Each fragment receives increment events via global SharedFlow and updates its local counter

## Key Formula Android Concepts Demonstrated

- **Fragment Key Management**: Using `FragmentKey` to identify fragments
- **Activity-Level Formula**: Managing global app state at the activity level with `NavigationActivityFormula`
- **Local State Management**: Each fragment managing its own state via Formula
- **Global Event System**: Using Kotlin Flows (`SharedFlow`) for cross-fragment communication
- **Compose Integration**: Using `ComposeViewFactory` to render fragment UI with Jetpack Compose (`CounterScreen`)
- **Activity-Orchestrated Navigation**: Activity performs navigation in response to events emitted by the formula via a `ViewModel`
- **Fragment Lifecycle**: How Formula manages fragment creation, state updates, and navigation
- **Event Subscription**: How fragments subscribe to global events that affect their local state

## Usage

Run the app and:
1. Start on Fragment 0 with counter at 0
2. Tap "Increment Counter for Fragment 0" to increment the current fragment's counter
3. Tap "Navigate to Next Fragment" to go to Fragment 1
4. Continue navigating to create more fragments
5. Use "Navigate Back" to return through the stack
6. Use "Increment Counter for Fragment X" buttons to send increment events to other fragments
7. Observe how each fragment's counter is managed locally but can be incremented from anywhere

To launch from Android Studio, run the `samples/navigation-fragments` app module. The main Activity is `NavigationActivity`.