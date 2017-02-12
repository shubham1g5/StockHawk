# StockHawk

StockHawk is an stocks tracking android application that also provides
an app widget to monitor your stocks.It allows you to add multiple stocks
and follow them. You can also see the history of the stocks through an interactive
graph.

## Implementation Features

- StockHawk uses Jake Wharton's [Butterknife library](http://jakewharton.github.io/butterknife/)
to inject views and bind data to them.
- It implements an app widget that lists the stocks added by you using Android Remote Views Adapter.
- It uses Yahoo Finance API to get latest stocks data.
- This project also demonstrates how you can convert legacy APIs that don't return RxJava Observables into reactive streams.
- It serves as an example of using Android ConstraintLayout to implement the app UI.
