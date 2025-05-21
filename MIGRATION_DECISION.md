# Why We Chose Firebase

## Main Reasons

1. **It's Free**
   - Firebase offers a generous free tier that covers all our current needs
   - No monthly costs unlike Azure which requires paid subscriptions
   - Can handle up to 50,000 reads and 20,000 writes per day for free
   - Perfect for our app's current scale

2. **Simple to Set Up**
   - No complex server configuration
   - Easy-to-use web console
   - Quick integration with Android

3. **Works Offline**
   - App keeps working even without internet
   - Data syncs automatically when connection returns

## Our Journey

We started looking at Azure, but it would have cost money and required building a whole backend API.

Then we tried using JSON files for testing, but this didn't give us real-time updates or offline support.

After watching some YouTube tutorials about Firebase, we realized it would be:
- Cheaper (free!)
- Faster to implement
- Easier to maintain

## Challenges We Faced

Moving to Firebase took some work:
- Learning how Firestore stores data
- Getting the document structure right
- Making sure IDs were handled correctly
- Updating the UI to handle real-time changes

## What's Left To Do

A few small things to finish up:
- Fix how the UI updates when data changes
- Add better error messages
- Clean up some old code

## Bottom Line

Firebase was the right choice because it's free, simple to use, and handles the offline capabilities we need without us having to build all that ourselves. 