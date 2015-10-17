# Popular-Movies
Android: This app displays a list of current popular/highest rated movies and lets you view its details (rating, synopsis, trailers, etc).

<ul>
<li>This app uses <code>Intents</code> to open up trailers in the YouTube app and <code>SharedPreferences</code> for storing favorites.</li>
<li>App has a <code>GridLayoutManager</code> for displaying movies in a grid and autofits to the size of the screen.
<li>Incorportates a Master Detail View for better use of space on tablets and compactness in phones.</li>
</ul>

***

### Notes:
API key has been removed from the source code! Please create your own API key by signing up at https://www.themoviedb.org.
Once you have your API key, please insert it in the `strings.xml` under the string: </br>
`<string name="themoviedb_api_key" translatable="false">INSERT_API_KEY</string>`

***

### Preview:

![](http://i.imgur.com/NqXV0tE.gif)

***

Icons are from [https://icons8.com/](https://icons8.com "icons8")

Designed for <b>Android JellyBean API 16+</b>
