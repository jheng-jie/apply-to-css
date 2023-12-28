# @applyToCSS use TailwindCSS

> This is an IntelliJ Plugin project.

<!-- Plugin description -->

## Mac Only

> Convert @apply to pure CSS syntax

> Pure use tailwindcss -i $file -c $config to extract the fields into the IDE.

- IDE requires Node configuration located at `Settings | Languages Frameworks | Node.js`

- Need to install Tailwind using the command `npm install tailwindcss -g`

#### Where to Use

> In old projects or projects without Tailwind CSS installed, use this when you want to apply atomic design principles.

#### How to Use

> In a CSS file, enter content starting with @apply on any line. Execute the shortcut Option + Enter and select Parse
> @apply to Pure CSS. This will automatically convert the content into pure CSS syntax.

### Setting Up Tailwind Config File

> `Setting | ApplyToCSS Setting | Config Path`

- [Configuration](https://tailwindcss.com/docs/configuration)

- Please specify the full path and include the filename field.

  ex: `/user/project/config.js`

- This can be used to convert units or add other properties.

<!-- Plugin description end -->


### Example

```css
// Press the shortcut and it will switch to the following.
@apply flex items-center p-4; 

// output:
display: flex;
align-items: center;
padding: 1rem;
```

### Config Example

```js
const ROOT_FONTSIZE = 100;

/** root font size to pixel */
const getPxToRem = (px, suffix = "rem") => {
  const unit = (px / ROOT_FONTSIZE).toFixed(4);
  return (unit.replace(/(\.?0+$)/g, "") + suffix).replace(/^0rem$/, "0");
};

/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/**/*.{js,ts,jsx,tsx,mdx,scss,css}"],
  theme: {
    extend: {
      spacing: {
        ...Array.from({ length: 1000 })
          .map((_, i) => i)
          .reduce((res, n) => ({ ...res, [n]: getPxToRem(n) }), {}),
      }
    }
  }
}
```

```css
// Press the shortcut and it will switch to the following.
@apply w-100 h-100 pt-10 pb-10;

// output:
height: 1rem;
width: 1rem;
padding-top: 0.1rem;
padding-bottom: 0.1rem;
```