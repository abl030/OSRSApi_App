# Old School RuneScape (OSRS) Market Analysis App

This application provides an analysis of the Old School RuneScape (OSRS) market, giving users insights into item prices, price differences, and potential Return on Investments (ROI) for flipping items.
Uses the price.runescape.wiki API for live price data.
## Features

- **Market Overview (MainActivity)**
    - Displays top 30 items, sorted by ROI.
    - Enables users to search for specific items using a search bar.
    - Each item is presented with its details like name, price difference, ROI, and more.

- **Item Detail View (ItemDetailActivity)**
    - Displays detailed market data for a specific item.
    - Provides high price, low price, and price difference charts.
    - Offers insights like average buy/sell time, potential profit per hour, suggested buy/sell prices, and more.

## Usage

### Market Overview

1. **Fetching Data**: The app starts by fetching the latest market data and mapping info.
2. **Data Processing**: Combines, sorts, filters, and processes the data to present the most relevant items.
3. **Item List Display**: Items are displayed in a RecyclerView.
4. **Search Functionality**: Users can search for specific items, which displays a filtered list in a separate RecyclerView.

### Item Detail View

1. **Retrieving Intent Data**: Gets item data passed from the overview.
2. **Fetching Time Series Data**: Grabs detailed time series data for the selected item.
3. **Updating Views**: Sets item-specific data to various TextViews, ImageView, etc.
4. **Chart Display**: Shows high price, low price, or price difference charts, updated based on user input (button clicks).

## Dependencies

- **Glide**: Used for loading item images from the OSRS wiki.
- **MPAndroidChart**: A library for displaying item price charts.
- **Retrofit**: A library for making API calls.
- **Gson**: A library for parsing JSON data.



## Installation

1. Clone the repository.
2. Build and run the application on an Android device or emulator.
