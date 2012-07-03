tags:
	ctags -R src

.PHONY: tags

clean:
	rm -f tags
