# Amazon Shopping Patches

Ad blocking, dark mode, price history charts, and Rufus tab removal for Amazon Shopping.

## Credits

Ad-blocking selectors, dark mode CSS fixes, and WebView injection approach are
inspired by and partially derived from **amznkiller** by [hxreborn](https://github.com/hxreborn/amznkiller)
(GPL-3.0). The selector list and dark-mode CSS are **vendored** in `AmazonHelper`:
nothing is fetched at runtime, so the JavaScript and CSS applied to an
authenticated Amazon page are fixed at build time. Refreshing the rules is a
reviewed commit plus a new patch bundle.

## Patches

| Patch | Default | Description |
|---|---|---|
| Remove ads | ✅ | Hides sponsored cards, video carousels, promo UI via CSS |
| Hide Rufus tab | ✅ | Removes AI assistant tab from bottom nav |
| Dark mode | ✅ | Off / Follow system / Always on |
| Disable video autoplay | ✅ | `WebSettings.setMediaPlaybackRequiresUserGesture(true)` at WebView creation |
| Disable search suggestions tracking | ✅ | Drops keypress/focus events from suggestion requests |
| Open links in browser | ✅ | Non-Amazon destinations (validated by parsed host) open externally |
| Fix Amazon manifest conflicts | ✅ | Namespaces Amazon-declared permissions so other Amazon apps coexist |
| Price history charts | ✅ | Keepa + CamelCamelCamel, loaded only after an explicit "Load price history" tap |

## Potential future patches

- **Remove "Frequently bought together"** — strip upsell section on product pages  
- **Unlock coupon visibility** — some coupons are A/B-gated; force-show coupon badge
- **Remove "Customers also viewed"** — strip recommendation carousels on product pages
- **Skip age/address confirmation dialogs** — auto-dismiss non-critical interstitials
