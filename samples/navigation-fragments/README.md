# Navigation Fragments Formula Sample

This sample demonstrates infinite fragment navigation using the Formula framework for UI state management.

## Features

- **Infinite Routing**: Start with fragment 0 and navigate to infinitely many fragments (1, 2, 3, ...)
- **Counter State**: Each fragment manages its own counter state locally in its Formula
- **Global Event System**: A global store manages counter increment events via Kotlin Flows
- **Fragment Navigation**: Each fragment can navigate to the next fragment or back in the navigation stack
- **Counter Management**: Each fragment can increment any fragment counter through the global event system
- **Formula Android Integration**: Demonstrates how Formula Android works with fragment navigation

## Architecture

### Core Components

1. **CounterFragmentKey**: Parcelable key that identifies fragments using integer IDs
2. **NavigationActivityFormula**: Activity-level Formula that manages:
   - Navigation state (current fragment, back stack)
   - Global counter increment events via SharedFlow
3. **CounterFragmentFormula**: Formula that manages individual fragment state including:
   - Local counter state
   - Counter increment event subscription for this fragment
4. **CounterFragmentFeatureFactory**: Creates Formula features for fragments
5. **CounterFragmentViewFactory**: Creates Android views for fragments

### State Management Architecture

**Activity-Level State:**

- Navigation stack managed in `NavigationActivityFormula.State`
- Counter increment events transmitted via `SharedFlow<Int>` within the activity formula
- Navigation actions handled through Formula effects

**Local State (per fragment):**

- Each fragment's counter is managed locally in its `CounterFragmentFormula.State`
- Counter starts at 0 and increments when global increment events are received for this fragment

### UI Elements

Each fragment contains:
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
- **Global Event System**: Using Kotlin Flows (SharedFlow) for cross-fragment communication
- **Navigation Effects**: Using effects to communicate navigation actions from activity formula to the activity
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