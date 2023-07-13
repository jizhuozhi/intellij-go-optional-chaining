# Optional Chaining

Plugin is avaliable on [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/22229-optional-chaining)

## Basic

Optional Chaining (Safe navigation) can be achieved by writing expressions in annotations.

A simple example:

```go
	f := func(a *A) (*E, error) {
		//optional a?.b()!.C()?!.D.Value()
	}
```

Through this plugin (select the comment, right click, and then click `Optional Chaining`, you can also customize the shortcut key) the following code will be generated

```go
	f := func(a *A) (*Value, error) {
		//optional a?.b()!.C()?!.D.Value()
		_0 := a
		if _0 == nil {
			return nil, nil
		}
		_1, err := _0.b()
		if err != nil {
			return nil, err
		}
		_2, err := _1.C()
		if err != nil || _2 == nil {
			return nil, err
		}
		return _2.D.Value(), nil
	}
```

You can see that expressions support four methods of getting values or executing functions
1. `.`: Use the same method of getting a value or executing a function as Go, and the statement will be translated in the original way
2. `?.`: Optional, if the value is `nil`, the function will end here and return the zero value of the corresponding type (if it is a numeric type, it is ` 0`, the pointer is `nil`, if it is a structure, it is `T{}`, and the zero value of other types defined in Go)
3. `!.`: Error-able, valid only for function calls, and requires the return value of the outer function to return two results, and the second result must be of type error. If the function call returns `err` is not `nil`, the function will end here and return an error
4. `?!.`: Optional and error-prone, the function is the combination of `?.` and `!.`

All statements except these four operators will be replaced with the original statement. (The plugin supports two types of function signatures `fun (...) T` and `fun (...) (T, error)`, the first type of function does not support < code>!.`)

## Comment format:

In order to avoid wrong parsing, the plugin requires that comments must be on a single line and written in the following format
`//optional ${your expression}` (replace `${your expression}` with your expression)
