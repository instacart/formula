# Navigation Fragments Formula Sample

This sample demonstrates infinite fragment navigation using the Formula framework for UI state management.

## Features

- **Infinite Routing**: Start with fragment 0 and navigate to infinitely many fragments (1, 2, 3, ...)
- **Global State Management**: A global store tracks integer counters for each fragment
- **Fragment Navigation**: Each fragment can navigate to the next fragment or back in the navigation stack
- **Counter Management**: Each fragment displays its own counter and provides buttons to increment counters of fragments in the back stack
- **Formula Android Integration**: Demonstrates how Formula Android works with fragment navigation

## Architecture

### Core Components

1. **NavigationFragmentKey**: Parcelable key that identifies fragments using integer IDs
2. **NavigationState**: Data class that holds the global state including:
    - Fragment counters map (fragment ID -> counter value)
    - Navigation stack (list of fragment IDs)
3. **NavigationStore**: RxJava-based store that manages the global navigation state
4. **NavigationFragmentFormula**: Formula that manages individual fragment state and handles user interactions
5. **NavigationFragmentFeatureFactory**: Creates Formula features for fragments
6. **NavigationFragmentViewFactory**: Creates Android views for fragments

### UI Elements

Each fragment contains:

- **Fragment Title**: Shows "Fragment X" where X is the fragment ID
- **Counter Display**: Shows the current counter value for this fragment
- **Navigate to Next Fragment Button**: Creates and navigates to the next fragment (ID + 1)
- **Navigate Back Button**: Goes back in the navigation stack (hidden for the root fragment)
- **Back Stack Counter Buttons**: Buttons for each fragment in the back stack that allow incrementing their counters

### Navigation Flow

1. App starts with Fragment 0
2. User can tap "Navigate to Next Fragment" to create Fragment 1, 2, 3, etc.
3. User can tap "Navigate Back" to go back through the navigation stack
4. User can tap counter increment buttons to modify the state of fragments in the back stack
5. All fragment states are preserved in the global store

## Key Formula Android Concepts Demonstrated

- **Fragment Key Management**: Using `FragmentKey` to identify fragments
- **Global State Management**: Sharing state across multiple fragments
- **Formula State Management**: Each fragment has its own Formula managing local UI state
- **Navigation Effects**: Using effects to communicate navigation actions from fragments to the activity
- **Fragment Lifecycle**: How Formula manages fragment creation, state updates, and navigation

## Usage

Run the app and:

1. Start on Fragment 0 with counter at 0
2. Tap "Navigate to Next Fragment" to go to Fragment 1
3. Continue navigating to create more fragments
4. Use "Navigate Back" to return through the stack
5. Use "Increment Counter for Fragment X" buttons to modify counters of previous fragments
6. Observe how state is preserved across navigation

This sample serves as preparation work for Jetpack Compose Navigation 3 support, demonstrating the core navigation patterns that will be
adapted for the future "navigation-nav3" sample.