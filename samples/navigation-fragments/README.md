# Navigation Fragments Formula Sample

This sample demonstrates infinite fragment navigation using the Formula framework for UI state management.

## Features

- **Infinite Routing**: Start with fragment 0 and navigate to infinitely many fragments (1, 2, 3, ...)
- **Counter State**: Each fragment manages its own counter state locally in its Formula
- **Global Event System**: `CounterStore` manages counter increment events and exposes the navigation stack via Kotlin Flows
- **Fragment Navigation**: Each fragment requests navigation via `CounterRouter` (Activity performs the transaction)
- **Counter Management**: Each fragment can increment any fragment counter through `CounterStore`
- **Compose UI in Fragments**: Fragment content is rendered with Jetpack Compose via `ComposeViewFactory`
- **Router Bridge**: Activity navigation is handled directly; fragments call `CounterRouter`, which delegates to the Activity

## Architecture

### Core Components

1. **CounterFragmentKey**: Parcelable key that identifies fragments using integer IDs
2. **NavigationActivity**: Directly performs fragment transactions and shows the initial fragment (`showNextFragment(0)`), handles back via
   `onBackPressed`
3. **NavigationActivityComponent**: Bridges `ActivityStoreContext` to fragments; provides `CounterStore` and `CounterRouter`; updates
   `CounterStore` with the current navigation stack
4. **CounterStore**: Global store exposing:
   - `counterIncrements(counterIndex: Int): Flow<Unit>` for increment events
   - `counterStack(): Flow<List<Int>>` reflecting the current fragment stack
   - `incrementCounterFor(counterIndex: Int)` and `updateCounterStack(stack: List<Int>)`
5. **CounterRouter**: Navigation interface (`onNavigateBack`, `onNavigateToNext(nextCounterIndex)`); `CounterRouterImpl` delegates to the
   Activity via `ActivityStoreContext`
6. **CounterFragmentFormula**: Manages per-fragment state including:
   - Local counter state
   - Subscriptions to `CounterStore.counterIncrements(counterIndex)` and `CounterStore.counterStack()`
   - Navigation via `CounterRouter`
7. **CounterFragmentFeatureFactory**: Binds fragment key to feature and injects `CounterStore` and `CounterRouter`
8. **CounterFragmentViewFactory**: Compose-based view factory for fragments (uses `CounterScreen`)
9. **CounterScreen**: Composable that renders the fragment UI

### State Management Architecture

**Activity-Level State:**

- `FragmentStore` provides `FragmentState` which is forwarded to `CounterStore` via a pre-render hook
- `CounterStore` holds the navigation stack and counter increment events (Kotlin Flows)
- Fragments request navigation through `CounterRouter`; the Activity executes transactions directly

**Local State (per fragment):**

- Each fragment's counter is managed locally in its `CounterFragmentFormula.State`
- Each fragment uses its `counterIndex` to subscribe to its increment events
- Counter starts at 0 and increments when `CounterStore` emits an increment for this fragment

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
6. Each fragment receives increment events via `CounterStore` and updates its local counter

## Key Formula Android Concepts Demonstrated

- **Local State Management**: Each fragment managing its own state via Formula
- **Global Event System**: Using `CounterStore` (Kotlin Flows) for cross-fragment communication and stack updates
- **Compose Integration**: Using `ComposeViewFactory` to render fragment UI with Jetpack Compose (`CounterScreen`)
- **Activity-Orchestrated Navigation**: Activity performs navigation directly; fragments invoke `CounterRouter`
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