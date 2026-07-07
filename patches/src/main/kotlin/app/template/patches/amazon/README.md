# Amazon Shopping Patches

Ad blocking, dark mode, price history charts, and Rufus tab removal for Amazon Shopping.

## Credits

Ad-blocking selectors, dark mode CSS/JS fixes, and WebView injection approach are
inspired by and partially derived from **amznkiller** by [hxreborn](https://github.com/hxreborn/amznkiller)
(GPL-3.0). Selector list and dark_mode.js are fetched live from the upstream repo.

## Patches

| Patch | Default | Description |
|---|---|---|
| Remove ads | ✅ | Hides sponsored cards, video carousels, promo UI via CSS |
| Hide Rufus tab | ✅ | Removes AI assistant tab from bottom nav |
| Dark mode | ❌ | Off / Follow system / Always on |
| Price history charts | ❌ | Keepa + CamelCamelCamel on product pages |

## Potential future patches

- **Disable in-app browser** — open product/deal links in default browser instead of WebView
- **Remove "Frequently bought together"** — strip upsell section on product pages  
- **Unlock coupon visibility** — some coupons are A/B-gated; force-show coupon badge
- **Disable video autoplay** — prevent product videos from autoplaying on /dp/ pages
- **Remove "Customers also viewed"** — strip recommendation carousels on product pages
- **Skip age/address confirmation dialogs** — auto-dismiss non-critical interstitials
